package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.UserService;
import service.GameService;
import service.AdminService;
import java.util.List;
import model.AuthData;
import model.GameData;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final AdminService adminService;

    private final Gson gson;

    public Server(){
        DataAccess dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.adminService = new AdminService(dataAccess);
        this.gson = new Gson();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint

        //Add endpoints here
        Spark.post("/user", (req, res) -> {
            var user = gson.fromJson(req.body(), model.UserData.class);
            var auth = userService.register(user);
            res.status(200);
            return gson.toJson(auth);
        });

        Spark.post("/session", (req, res) -> {
           var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
           var auth = userService.login(loginRequest.username(), loginRequest.password());
           res.status(200);
           return gson.toJson(auth);
        });

        Spark.delete("/session", (req, res) -> {
            String authToken = req.headers("authorization");
            userService.logout(authToken);
            res.status(200);
            return "{}";
        });

        Spark.get("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            var games = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(new ListGameResponse(games));
        });

        Spark.post("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            var createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            int gameID = gameService.createGame(authToken, createGameRequest.gameName());
            res.status(200);
            return gson.toJson(new CreateGameResponse(gameID));
        });

        Spark.put("/game", (req, res) -> {
            String authToken = req.headers("authorization");
            var joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, joinGameRequest.playerColor(), joinGameRequest.gameID());
            res.status(200);
            return "{}";
        });

        Spark.delete("/db", (req, res) -> {
            adminService.clearApplication();
            res.status(200);
            return "{}";
        });

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(new ErrorResponse(e.getMessage())));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private record LoginRequest(String username, String password) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record CreateGameResponse(int gameID) {}
    private record ListGameResponse(List<GameData> games) {}
    private record ErrorResponse(String message) {}

}

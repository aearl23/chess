package server;

import model.UserData;
import spark.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.UserService;
import service.GameService;
import service.AdminService;
import java.util.List;
import model.GameData;
import server.RegisterHandler;
import server.JoinGameHandler;
import server.LoginHandler;

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

        // Register your handlers
        RegisterHandler registerHandler  = new server.RegisterHandler(userService);
        LoginHandler loginHandler = new server.LoginHandler(userService);
        LogoutHandler logoutHandler = new server.LogoutHandler(userService);
        JoinGameHandler joinGameHandler = new server.JoinGameHandler(gameService);
        ClearHandler clearHandler = new server.ClearHandler(adminService);


        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);
        Spark.put("/game", joinGameHandler);
        Spark.put("/session", logoutHandler);
        Spark.put("/session", clearHandler);


        //This line initializes the server and can be removed once you have a functioning endpoint

        //Add endpoints here
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
    }

    private record CreateGameRequest(String gameName) {}
    private record CreateGameResponse(int gameID) {}
    private record ListGameResponse(List<GameData> games) {}
    private record ErrorResponse(String message) {}

}

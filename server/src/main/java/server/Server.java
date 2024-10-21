package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.UserService;
import service.GameService;
import service.AdminService;
import java.util.List;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final AdminService adminService;

    pirvate final Gson gson;

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
    private record ListGameResponse(List<model.GameData> games) {}
    private record ErrorResponse(String message) {}

}

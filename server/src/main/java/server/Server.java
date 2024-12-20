package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySqlDataAccess;
import server.websocket.WebSocketHandler;
import model.*;
import service.*;
import spark.*;
import java.util.List;

public class Server {
    private final UserService userService;
    private final GameService gameService;
    private final AdminService adminService;
    private final WebSocketHandler webSocketHandler;

    private final Gson gson;

    public Server(){
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();

            DataAccess dataAccess = new MySqlDataAccess();

            this.userService = new UserService(dataAccess);
            this.gameService = new GameService(dataAccess);
            this.adminService = new AdminService(dataAccess);
            this.webSocketHandler = new WebSocketHandler(gameService);
            this.gson = new Gson();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.webSocket("/ws", webSocketHandler);

        Spark.staticFiles.location("web");

//        Spark.init();

        // Register your handlers
        RegisterHandler registerHandler  = new RegisterHandler(userService);
        LoginHandler loginHandler = new LoginHandler(userService);
        LogoutHandler logoutHandler = new LogoutHandler(userService);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(adminService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);


        Spark.post("/user", registerHandler);
        Spark.post("/session", loginHandler);
        Spark.put("/game", joinGameHandler);
        Spark.delete("/session", logoutHandler);
        Spark.delete("/db", clearHandler);
        Spark.get("/game", listGamesHandler);
        Spark.post("/game", createGameHandler);
        //Add endpoints here

        Spark.exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body(gson.toJson(new ErrorResponse(e.getMessage())));
        });

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        // Close all active WebSocket sessions
        webSocketHandler.closeAllSessions();
        Spark.stop();
    }

    private record CreateGameRequest(String gameName) {}
    private record CreateGameResponse(int gameID) {}
    private record ListGameResponse(List<GameData> games) {}
    private record ErrorResponse(String message) {}

}

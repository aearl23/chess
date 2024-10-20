package server;

import spark.*;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.UserService;

public class Server {
    private final UserService userService;

    public Server(){
        DataAccess dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint

        //Add endpoints here
        Spark.post("/user", new "RegisterHandler"(userService));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}

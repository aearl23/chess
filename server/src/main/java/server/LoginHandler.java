package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import model.UserData;
import model.AuthData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService;
    private final Gson gson;

    public LoginHandler(UserService userService){
        this.userService = userService;
        this.gson = new Gson();
    }

    @Override
    public Object handle(Request request, Response response){
        try {
          //parse the request body
          LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

          if (loginRequest == null || loginRequest.username() == null || loginRequest.password() == null) {
              response.status(400);
              return gson.toJson(new ErrorResponse("Error: bad request"));
          }

          UserData loginData = new UserData(loginRequest.username(), loginRequest.password(), null);
          AuthData auth = userService.login(loginData);

          //successful login
          response.status(200);
          return gson.toJson(auth);

        } catch (DataAccessException e) {
            String message = e.getMessage();

            if (message.contains("invalid username")) {
              response.status(401);
              return gson.toJson(new ErrorResponse("Error: invalid username"));
            }

            response.status(500);
            return gson.toJson(new ErrorResponse("Error: " + message));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
    private record LoginRequest(String username, String password){}
    private record ErrorResponse(String message){}
}

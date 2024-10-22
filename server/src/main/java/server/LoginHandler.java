package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route{
    private final UserService userService;
    private final Gson gson;

    public LoginHandler(UserService userService){
        this.userService = userService;
        this.gson = new Gson();
    }

    @Override
    public Object handle(Request request, Response response){
        var loginRequest = gson.fromJson(request.body(), UserData.class);

        try {
          AuthData authData = userService.login(loginRequest);
          response.status(200);
          return gson.toJson(authData);
        } catch (DataAccessException e){
          response.status(401);
          return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private static class ErrorResponse {
      private final String message;

      public ErrorResponse(String message){
          this.message = message;
      }
    }
}

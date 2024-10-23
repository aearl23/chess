package server;

import com.google.gson.Gson;
import dataaccess.InvalidUsernameException;
import dataaccess.WrongPasswordException;
import dataaccess.DatabaseException;
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
        response.type("application/json");
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

        } catch (InvalidUsernameException e) {
          // Handle invalid username error
          response.status(401); // Unauthorized
          return gson.toJson(new ErrorResponse(e.getMessage()));

        } catch (WrongPasswordException e) {
          // Handle wrong password error
          response.status(401); // Unauthorized
          return gson.toJson(new ErrorResponse(e.getMessage()));

        } catch (DatabaseException e) {
          // Handle any other database-related errors
          response.status(500); // Internal Server Error
          return gson.toJson(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
          // Handle any other unexpected exceptions
          response.status(500); // Internal Server Error
          return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
    private record LoginRequest(String username, String password){}
    private record ErrorResponse(String message){}
}
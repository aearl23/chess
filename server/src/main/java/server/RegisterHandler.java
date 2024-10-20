package server;

import com.google.gson.Gson;
import model.UserData;
import model.AuthData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterHandler implements Route {
  private final UserService userService;
  private final Gson gson;

  public RegisterHandler(UserService userService){
    this.userService = userService;
    this.gson = new Gson();
  }

  @Override
  public Object handle(Request request, Response response){
    UserData userData = gson.fromJson(request.body(), UserData.class);

    try {
      AuthData authData = userService.register(userData);
      response.status(200);
      return gson.toJson(authData);
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

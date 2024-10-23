package server;

import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route {
  private final UserService userService;
  private final Gson gson;

  public LogoutHandler(UserService userService) {
    this.userService = userService;
    this.gson = new Gson();
  }

  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");
    try {
      // Get authToken from headers
      String authToken=request.headers("authorization");
      // Call service method to logout
      userService.logout(authToken);
      // Successful logout
      response.status(200);
      return "{}";

    } catch (UnauthorizedException e) {
      response.status(401);
      return gson.toJson(new ErrorResponse("Error: unauthorized"));
    } catch (Exception e) {
      // Handle any other unexpected exceptions
      response.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  }

  private record ErrorResponse(String message) {}
}

package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.AdminService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route {
  private final AdminService adminService;
  private final Gson gson;

  public ClearHandler(AdminService adminService) {
    this.adminService = adminService;
    this.gson = new Gson();
  }

  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");
    try {
      // Check for authorization header
      String authToken = request.headers("authorization");
      if (authToken == null || authToken.isEmpty()) {
        response.status(401);
        return gson.toJson(new ErrorResponse("Error: unauthorized"));
      }

      // Call service method to clear application
      adminService.clearApplication();

      // Successful clear
      response.status(200);
      return "{}";

    } catch (DataAccessException e) {
      response.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
      response.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  }

  private record ErrorResponse(String message) {}
}

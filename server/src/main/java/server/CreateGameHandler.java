package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
  private final GameService gameService;
  private final Gson gson;

  public CreateGameHandler(GameService gameService) {
    this.gameService = gameService;
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

      // Parse request body
      CreateGameRequest createRequest = gson.fromJson(request.body(), CreateGameRequest.class);

      // Validate request body and game name
      if (createRequest == null || createRequest.gameName() == null || createRequest.gameName().isEmpty()) {
        response.status(400);
        return gson.toJson(new ErrorResponse("Error: bad request"));
      }

      // Call service method to create game
      int gameId = gameService.createGame(authToken, createRequest.gameName());

      // Successful creation
      response.status(200);
      return gson.toJson(new CreateGameResponse(gameId));

    } catch (UnauthorizedException e) {
      response.status(401);
      return gson.toJson(new ErrorResponse("Error: unauthorized"));
    } catch (DataAccessException e) {
      response.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    } catch (Exception e) {
      response.status(500);
      return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
    }
  }

  private record CreateGameRequest(String gameName) {}
  private record CreateGameResponse(int gameID) {}
  private record ErrorResponse(String message) {}
}

package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.List;

public class ListGamesHandler implements Route {
  private final GameService gameService;
  private final Gson gson;

  public ListGamesHandler(GameService gameService) {
    this.gameService = gameService;
    this.gson = new Gson();
  }

  @Override
  public Object handle(Request request, Response response) {
    response.type("application/json");
    try {
      // Check for authorization header
      String authToken = request.headers("authorization");
      // Call service method to get games
      List<GameData> games = gameService.listGames(authToken);

      // Successful retrieval
      response.status(200);
      return gson.toJson(new ListGamesResponse(games));

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

  private record ListGamesResponse(List<GameData> games) {}
  private record ErrorResponse(String message) {}
}

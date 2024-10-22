package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson;

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson();
    }
    @Override
    public Object handle(Request request, Response response) {
      //grab authtoken from headers
      String authToken = request.headers("authorization");
      if (authToken == null || authToken.isEmpty()){
        response.status(401);
        return gson.toJson(new ErrorResponse("Error: unauthorized"));
      }

      try{
          //parse request body
          var joinRequest = gson.fromJson(request.body(), JoinGameRequest.class)
          //call the service to join the game
          gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());
          response.status(200);
          return "{}"; //Empty json object for success
      } catch (DataAccessException e) {
          if (e.getMessage().contains("unauthorized")){
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
          } else if (e.getMessage().contains("already taken")) {
              response.status(403);
              return gson.toJson(new ErrorResponse("Error: already taken"));
          } else if (e.getMessage().contains("bad request")) {
              response.status(400);
              return gson.toJson(new ErrorResponse("Error: bad request"));
          }
          response.status(500);
          return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
      }
  }
  private record JoinGameRequest(String playerColor, int gameID) {
  }

  private static class ErrorResponse{
      private final String message;

      public ErrorResponse(String message){
          this.message = message;
      }
  }
}

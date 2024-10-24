package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.GameAlreadyTakenException;
import service.GameService;
import dataaccess.UnauthorizedException;
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
      try{
        //parse request body
        var joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);

        if (authToken == null || authToken.isEmpty()){
          response.status(401);
          return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }
        //call the service to join the game
        gameService.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
        response.status(200);
        return "{}"; //Empty json object for success

      } catch (BadRequestException e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
      } catch (UnauthorizedException e){
            response.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
      } catch (GameAlreadyTakenException e) {
              response.status(403);
              return gson.toJson(new ErrorResponse("Error: already taken"));
      } catch (Exception e) {
          response.status(500);
          return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
      }
    }
  private record JoinGameRequest(String playerColor, int gameID) {
  }

  private static class ErrorResponse {
      private final String message;

      public ErrorResponse(String message){
          this.message = message;
      }
  }
}

package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
  private final DataAccess dataAccess;

  public GameService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public List<GameData> listGames(String authToken) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }
    return dataAccess.listGames();
  }

  public int createGame(String authToken, String gameName) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }
    GameData newGame = new GameData(0, null, null, gameName, new ChessGame());
    return dataAccess.createGame(newGame);
  }

  public void joinGame(String authToken, String playerColor, int gameID) throws BadRequestException,
          UnauthorizedException, GameAlreadyTakenException {
      AuthData auth = dataAccess.getAuth(authToken);
      if (auth == null) {
          throw new UnauthorizedException("Error: unauthorized");
      }

      GameData game = dataAccess.getGame(gameID);
      if (game == null) {
          throw new BadRequestException("Error: invalid game ID");
      }


      if (playerColor == null){
        throw new BadRequestException("Error: invalid player color");
      } else if (playerColor.equals("WHITE")) {
          if (game.whiteUsername() != null) {
              throw new GameAlreadyTakenException("Error: White player slot already taken");
          }
            dataAccess.updateGame( new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game()));
      } else if (playerColor.equals("BLACK")) {
          if (game.blackUsername() != null) {
              throw new GameAlreadyTakenException("Error: Black player slot already taken");
          }
        dataAccess.updateGame( new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game()));
      }
    }
}


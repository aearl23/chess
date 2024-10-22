package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import java.util.List;

public class GameService {
  private final DataAccess dataAccess;

  public GameService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public List<GameData> listGames(String authToken) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    return dataAccess.listGames();
  }

  public int createGame(String authToken, String gameName) throws DataAccessException {
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    GameData newGame = new GameData(0, null, null, gameName, new ChessGame());
    return dataAccess.createGame(newGame);
  }

  public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new DataAccessException("Error: unauthorized");
    }

    GameData game = dataAccess.getGame(gameID);
    if (game == null) {
      throw new DataAccessException("Error: bad request");
    }

    GameData updatedGame;
    if (playerColor.equalsIgnoreCase("WHITE")) {
      if (game.whiteUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      updatedGame = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
    } else if (playerColor.equalsIgnoreCase("BLACK")) {
      if (game.blackUsername() != null) {
        throw new DataAccessException("Error: already taken");
      }
      updatedGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
    } else {
      throw new DataAccessException("Error: bad request");
    }

    dataAccess.updateGame(updatedGame);
  }
}


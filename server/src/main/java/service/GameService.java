package service;

import chess.*;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import server.websocket.WebSocketHandler;
import java.util.List;

public class GameService {
  private final DataAccess dataAccess;
  private final WebSocketHandler webSocketHandler;


  public GameService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
    this.webSocketHandler = new WebSocketHandler(this);
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
          UnauthorizedException, GameAlreadyTakenException, DataAccessException {
      AuthData auth = dataAccess.getAuth(authToken);
      if (auth == null) {
          throw new UnauthorizedException("Error: unauthorized");
      }

      GameData game = dataAccess.getGame(gameID);
      if (game == null) {
          throw new BadRequestException("Error: invalid game ID");
      }

      if (playerColor == null || playerColor.isEmpty()) {
        throw new BadRequestException("Error: player color is required");
      }

      GameData updatedGame;
      switch (playerColor.toUpperCase()) {
        case "WHITE" -> {
          if (game.whiteUsername() != null) {
            throw new GameAlreadyTakenException("Error: already taken");
          }
          updatedGame = new GameData(game.gameID(), auth.username(), game.blackUsername(),
                  game.gameName(), game.game());
        }
        case "BLACK" -> {
          if (game.blackUsername() != null) {
            throw new GameAlreadyTakenException("Error: already taken");
          }
          updatedGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(),
                  game.gameName(), game.game());
        }
        default -> throw new BadRequestException("Error: invalid player color");
      }
      dataAccess.updateGame(updatedGame);
  }

  public AuthData getAuth(String authToken) throws Exception {
    try {
      // Try to get the auth data from the database
      AuthData auth = dataAccess.getAuth(authToken);
      if (auth == null) {
        throw new UnauthorizedException("Invalid auth token");
      }
      return auth;
    } catch (DataAccessException e) {
      throw new UnauthorizedException("Error validating auth token");
    }
  }


  public GameData getGame(int gameID) throws DataAccessException {
    GameData game = dataAccess.getGame(gameID);
    if (game == null) {
      throw new BadRequestException("Error: game not found");
    }
    return game;
  }

  public GameData makeMove(int gameID, ChessMove move) throws DataAccessException, InvalidMoveException{
    GameData gameData = getGame(gameID);
    ChessGame game = gameData.game();
    // make move
    game.makeMove(move);
    // Update game in database
    dataAccess.updateGame(gameData);
    return gameData;
  }

  public GameData resignGame(int gameID, String username) throws DataAccessException {
    GameData gameData = getGame(gameID);

    // Verify player is in the game
    if (!username.equals(gameData.whiteUsername()) &&
            !username.equals(gameData.blackUsername())) {
      throw new UnauthorizedException("Error: not a player in this game");
    }

    // Mark game as over (you might want to add a field to GameData for this)
    ChessGame game = gameData.game();

    game.setGameOver(true);
    dataAccess.updateGame(gameData);
    return gameData;
  }

  public boolean isPlayerInGame(int gameID, String authToken) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      return false;
    }

    GameData game = getGame(gameID);
    return auth.username().equals(game.whiteUsername()) ||
            auth.username().equals(game.blackUsername());
  }

  public boolean isGameOver(int gameID) throws DataAccessException {
    GameData game = getGame(gameID);
    ChessGame chessGame = game.game();

    // Check for checkmate or stalemate
    return chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
            chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
            chessGame.isInStalemate(chessGame.getTeamTurn());
  }

  public String getPlayerColor(int gameID, String authToken) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }

    GameData game = getGame(gameID);
    if (auth.username().equals(game.whiteUsername())) {
      return "WHITE";
    } else if (auth.username().equals(game.blackUsername())) {
      return "BLACK";
    } else {
      return null; // Observer
    }
  }

  public void updateGame(GameData game) throws DataAccessException{
    dataAccess.updateGame(game);
  }

  public String verifyAuth(String authToken) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }
    return auth.username();
  }
}


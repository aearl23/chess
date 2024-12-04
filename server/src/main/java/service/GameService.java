package service;

import chess.*;
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


  public GameData getGame(int gameID) throws DataAccessException {
    GameData game = dataAccess.getGame(gameID);
    if (game == null) {
      throw new BadRequestException("Error: game not found");
    }
    return game;
  }

  public void makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException {
    // Verify auth and get game
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }

    GameData gameData = getGame(gameID);
    ChessGame game = gameData.game();

    // Verify it's the player's turn
    boolean isWhitePlayer = auth.username().equals(gameData.whiteUsername());
    boolean isBlackPlayer = auth.username().equals(gameData.blackUsername());

    if (!isWhitePlayer && !isBlackPlayer) {
      throw new UnauthorizedException("Error: not a player in this game");
    }

    if ((game.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhitePlayer) ||
            (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlackPlayer)) {
      throw new BadRequestException("Error: not your turn");
    }

    // Validate and make the move
    ChessPosition start = move.getStartPosition();
    ChessPiece piece = game.getBoard().getPiece(start);

    if (piece == null || piece.getTeamColor() != game.getTeamTurn()) {
      throw new BadRequestException("Error: invalid piece selection");
    }

    if (!game.validMoves(start).contains(move)) {
      throw new BadRequestException("Error: invalid move");
    }

    game.makeMove(move);

    // Update game in database
    dataAccess.updateGame(gameData);
  }

  public void resignGame(int gameID, String authToken) throws DataAccessException {
    AuthData auth = dataAccess.getAuth(authToken);
    if (auth == null) {
      throw new UnauthorizedException("Error: unauthorized");
    }

    GameData gameData = getGame(gameID);

    // Verify player is in the game
    if (!auth.username().equals(gameData.whiteUsername()) &&
            !auth.username().equals(gameData.blackUsername())) {
      throw new UnauthorizedException("Error: not a player in this game");
    }

    // Mark game as over (you might want to add a field to GameData for this)
    ChessGame game = gameData.game();
    // Set the game state to indicate resignation
    // This might require adding a method to your ChessGame class
    // game.setGameOver() or similar

    dataAccess.updateGame(gameData);
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
}


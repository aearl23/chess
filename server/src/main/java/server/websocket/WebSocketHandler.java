package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.messages.*;
import websocket.commands.UserGameCommand;

@WebSocket
public class WebSocketHandler {
  private final ConnectionManager connectionManager;
  private final GameService gameService;
  private final Gson gson;
  private Session session;

  public WebSocketHandler(GameService gameService) {
    this.gameService = gameService;
    this.connectionManager = new ConnectionManager();
    this.gson = new Gson();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    // Connection established
    this.session = session;
    System.out.println("WebSocket connection opened");
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    // Connection closed
    try {
      ConnectionManager.Connection connection = connectionManager.getConnection(session);
      if (connection != null) {
        handleLeave(session);
      }
    } catch (Exception e) {
      System.err.println("Error during connection close: " + e.getMessage());
    } finally {
         this.session = null;
    }
  }

  @OnWebSocketError
  public void onError(Throwable error) {
    System.err.println("WebSocket error: " + error.getMessage());
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    this.session = session;
    try {
        handleMessage(message, session);
    } catch (Exception e) {
      // Need to handle error differently since we don't have session
        sendError(session, "Error processing message: " + e.getMessage());
    }
  }

  public void handleMessage(String message, Session session) {
    try {
      UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

      switch (command.getCommandType()) {
        case CONNECT -> handleConnect(command, session);
        case MAKE_MOVE -> handleMove(command, session);
        case LEAVE -> handleLeave(session);
        case RESIGN -> handleResign(session);
        default -> sendError(session, "Unknown command type");
      }
    } catch (Exception e) {
      sendError(session, "Error processing command: " + e.getMessage());
    }
  }

  private void handleConnect(UserGameCommand command, Session session) {
    if (session == null || !session.isOpen()) {
      System.err.println("No active session for connect command");
      return;
    }

    try {
      Integer gameId = command.getGameID();
      String authToken = command.getAuthToken();

      // Validate auth token and get username
      AuthData auth = gameService.getAuth(authToken);
      String username = auth.username();

      // Get game data
      GameData game = gameService.getGame(gameId);
      if (game == null) {
        throw new Exception("Game not found");
      }

      // Determine player's color
      ChessGame.TeamColor playerColor = null;
      if (username.equals(game.whiteUsername())) {
        playerColor = ChessGame.TeamColor.WHITE;
      } else if (username.equals(game.blackUsername())) {
        playerColor = ChessGame.TeamColor.BLACK;
      }

      // Add connection
      connectionManager.add(gameId, username, session, playerColor);

      // Send initial game state
      LoadGameMessage loadGameMessage = new LoadGameMessage(game);
      session.getRemote().sendString(gson.toJson(loadGameMessage));

      // Notify others
      String joinMessage = username + " joined the game" +
              (playerColor == null ? " as an observer" : " as " + playerColor);
      connectionManager.broadcast(gameId, username, new NotificationMessage(joinMessage));

    } catch (Exception e) {
      sendError(session, "Connection failed: " + e.getMessage());
    }
  }

  private void handleMove(UserGameCommand command, Session session) {
    if (session == null || !session.isOpen()) {
      System.err.println("No active session for move command");
      return;
    }

    try {
      // First validate the auth token
      String authToken = command.getAuthToken();
      try {
        // This will throw an exception if auth token is invalid
        gameService.getAuth(authToken);
      } catch (Exception e) {
          sendError(session, "Invalid auth token");
          return;
      }

      ConnectionManager.Connection connection = connectionManager.getConnection(session);
      if (connection == null) {
          sendError(session, "Not connected to game");
          return;
      }

      Integer gameId = connection.gameId();
      GameData game = gameService.getGame(gameId);
      if (game == null) {
        sendError(session, "Game not found");
        return;
      }

      // Check if game is already over
      if (game.game().isGameOver()) {
        sendError(session, "Game is already over");
        return;
      }

      ChessMove move = command.getMove();
      // Validate that it's the player's turn and they're moving their own piece
      ChessPiece piece = game.game().getBoard().getPiece(move.getStartPosition());

      // First check if this is actually a player (not an observer)
      ChessGame.TeamColor playerColor = connection.playerColor();
      if (playerColor == null) {
        sendError(session, "Observers cannot make moves");
        return;
      }

      // Check if it's this player's turn
      if (game.game().getTeamTurn() != playerColor) {
        sendError(session, "Not your turn");
        return;
      }

      //check piece existence
      if (piece == null) {
          sendError(session,"No piece at start position");
          return;
      }


      // Check if player is trying to move opponent's piece
      if (piece.getTeamColor() != playerColor) {
        sendError(session, "Cannot move opponent's pieces");
        return;
      }


      try {
          //fix makeMove error
          GameData updatedGame = gameService.makeMove(connection.gameId(), move);
          // Send updates only if move was successful
          LoadGameMessage loadGameMessage = new LoadGameMessage(updatedGame);
          //Send Load Game message to all players
          connectionManager.broadcastToGame(connection.gameId(), loadGameMessage);


        // Notify about the move
          String moveNotification = connection.username() + " made a move";
          // If game is now over after this move, add that to the notification
          if (updatedGame.game().isGameOver()) {
            ChessGame.TeamColor winner = updatedGame.game().getWinner();
            if (winner != null) {
              moveNotification += ". Game Over! " + winner + " wins!";
            } else {
              moveNotification += ". Game Over! It's a draw!";
            }
          }
          connectionManager.broadcast(connection.gameId(), connection.username(), new NotificationMessage(moveNotification));
      } catch (Exception e) {
        // If the move is invalid, send error and return
        sendError(session, e.getMessage());
      }
    } catch (Exception e) {
        sendError(session, e.getMessage());
    }
  }

  private void handleLeave(Session session) {
    if (session == null || !session.isOpen()) {
      System.err.println("No active session for leave command");
      return;
    }

    try {
      ConnectionManager.Connection connection = connectionManager.getConnection(session);
      if (connection != null) {
        GameData game = gameService.getGame(connection.gameId());

        // Create updated game data with the leaving player removed
        GameData updatedGame;
        if (connection.playerColor() == ChessGame.TeamColor.WHITE) {
          updatedGame = new GameData(
                  game.gameID(),
                  null,  // Remove white player
                  game.blackUsername(),
                  game.gameName(),
                  game.game()
          );
        } else if (connection.playerColor() == ChessGame.TeamColor.BLACK) {
          updatedGame = new GameData(
                  game.gameID(),
                  game.whiteUsername(),
                  null,  // Remove black player
                  game.gameName(),
                  game.game()
          );
        } else {
          // Observer leaving doesn't need to update game data
          updatedGame = game;
        }

        // Update the game in the database
        gameService.updateGame(updatedGame);

        String leaveMessage = connection.username() + " left the game";
        connectionManager.broadcast(connection.gameId(), connection.username(),
                new NotificationMessage(leaveMessage));
        connectionManager.remove(connection.username());
      }
    } catch (Exception e) {
      sendError(session, "Error leaving game: " + e.getMessage());
    }
  }

  private void handleResign(Session session) {
    if (session == null || !session.isOpen()) {
      System.err.println("No active session for resign command");
      return;
    }

    try {
      ConnectionManager.Connection connection = connectionManager.getConnection(session);
      if (connection == null) {
        sendError(session, "Not connected to game");
        return;
      }

      // Check if player is an observer
      if (connection.playerColor() == null) {
        sendError(session, "Observers cannot resign");
        return;
      }

      GameData game = gameService.getGame(connection.gameId());
      // Check if game is already over
      if (game.game().isGameOver()) {
        sendError(session, "Game is already over");
        return;
      }
      // Process resignation
      game = gameService.resignGame(connection.gameId(), connection.username());

      // Notify about resignation
      String resignMessage = connection.username() + " resigned from the game";
      NotificationMessage notification = new NotificationMessage(resignMessage);
      LoadGameMessage loadGameMessage = new LoadGameMessage(game);

      // Send notification to the resigning player
      session.getRemote().sendString(gson.toJson(notification));

      // Broadcast to others
      connectionManager.broadcast(connection.gameId(), connection.username(), notification);

    } catch (Exception e) {
      sendError(session, "Error resigning game: " + e.getMessage());
    }
  }

  private void sendError(Session session, String message) {
    try {
      ErrorMessage errorMessage = new ErrorMessage("Error: " + message);
      session.getRemote().sendString(gson.toJson(errorMessage));
    } catch (Exception e) {
      System.err.println("Error sending error message: " + e.getMessage());
    }
  }

  public void closeAllSessions() {
    connectionManager.cleanupInactiveSessions();
  }


  class ContextProvider {
    private static final ThreadLocal<Session> currentSession = new ThreadLocal<>();

    public static void setCurrentSession(Session session) {
      currentSession.set(session);
    }

    public static Session getCurrentSession() {
      return currentSession.get();
    }

    public static void clearCurrentSession() {
      currentSession.remove();
    }
  }
}
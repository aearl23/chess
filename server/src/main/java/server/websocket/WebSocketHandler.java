package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.Gson;
import chess.ChessGame;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@WebSocket
public class WebSocketHandler {
  private final Map<Session, Integer> sessionToGame = new ConcurrentHashMap<>();
  private final GameService gameService;
  private final Gson gson;

  public WebSocketHandler(GameService gameService) {
    this.gameService = gameService;
    this.gson = new Gson();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    // Just store the session - we'll associate it with a game when we receive the CONNECT command
    sessionToGame.put(session, null);
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    Integer gameId = sessionToGame.get(session);
    if (gameId != null) {
      // Notify other players in the game that this player left
      NotificationMessage message = new NotificationMessage("A player has left the game");
      broadcastToGame(gameId, message, session);
    }
    sessionToGame.remove(session);
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {
    try {
      UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

      switch (command.commandType) {
        case CONNECT:
          handleConnect(session, command);
          break;
        case MAKE_MOVE:
          handleMove(session, gson.fromJson(message, MakeMoveCommand.class));
          break;
        case LEAVE:
          handleLeave(session, command);
          break;
        case RESIGN:
          handleResign(session, command);
          break;
        default:
          sendError(session, "Unrecognized command type");
      }
    } catch (Exception e) {
      sendError(session, "Error processing message: " + e.getMessage());
    }
  }

  private void handleConnect(Session session, UserGameCommand command) {
    try {
      // Validate game exists
      Game game = gameService.getGame(command.gameID);
      if (game == null) {
        sendError(session, "Game not found");
        return;
      }

      // Associate session with game
      sessionToGame.put(session, command.gameID);

      // Send current game state to connecting client
      LoadGameMessage loadMessage = new LoadGameMessage(game);
      send(session, loadMessage);

      // Notify others of connection
      NotificationMessage notification = new NotificationMessage("New player connected to the game");
      broadcastToGame(command.gameID, notification, session);
    } catch (Exception e) {
      sendError(session, "Error connecting to game: " + e.getMessage());
    }
  }

  private void handleMove(Session session, MakeMoveCommand command) {
    try {
      Game game = gameService.makeMove(command.gameID, command.move);

      // Broadcast updated game state
      LoadGameMessage loadMessage = new LoadGameMessage(game);
      broadcastToGame(command.gameID, loadMessage, null);

      // Send move notification
      NotificationMessage notification = new NotificationMessage("A move was made");
      broadcastToGame(command.gameID, notification, session);

      // Check for game state changes
      if (game.isInCheck()) {
        NotificationMessage checkNotification = new NotificationMessage("Check!");
        broadcastToGame(command.gameID, checkNotification, null);
      }
      if (game.isInCheckmate()) {
        NotificationMessage checkmateNotification = new NotificationMessage("Checkmate!");
        broadcastToGame(command.gameID, checkmateNotification, null);
      }
    } catch (Exception e) {
      sendError(session, "Invalid move: " + e.getMessage());
    }
  }

  private void handleLeave(Session session, UserGameCommand command) {
    Integer gameId = sessionToGame.get(session);
    if (gameId != null) {
      NotificationMessage notification = new NotificationMessage("A player has left the game");
      broadcastToGame(gameId, notification, session);
      sessionToGame.remove(session);
    }
  }

  private void handleResign(Session session, UserGameCommand command) {
    try {
      Game game = gameService.resignGame(command.gameID, command.authToken);

      // Broadcast game ended
      LoadGameMessage loadMessage = new LoadGameMessage(game);
      broadcastToGame(command.gameID, loadMessage, null);

      // Notify of resignation
      NotificationMessage notification = new NotificationMessage("A player has resigned");
      broadcastToGame(command.gameID, notification, null);
    } catch (Exception e) {
      sendError(session, "Error processing resignation: " + e.getMessage());
    }
  }

  private void send(Session session, ServerMessage message) {
    try {
      session.getRemote().sendString(gson.toJson(message));
    } catch (IOException e) {
      System.err.println("Error sending message: " + e.getMessage());
    }
  }

  private void sendError(Session session, String message) {
    ErrorMessage errorMessage = new ErrorMessage(message);
    send(session, errorMessage);
  }

  private void broadcastToGame(int gameId, ServerMessage message, Session exclude) {
    for (Map.Entry<Session, Integer> entry : sessionToGame.entrySet()) {
      if (gameId == entry.getValue() &&
              (exclude == null || !entry.getKey().equals(exclude))) {
        send(entry.getKey(), message);
      }
    }
  }
}
package client.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;
import javax.websocket.*;
import java.net.URI;
import java.io.IOException;

@ClientEndpoint
public class WebSocketCommunicator {
  private final ServerMessageObserver observer;
  private final Gson gson;
  private Session session = null;
  private static final long TIMEOUT_MS = 10000; // 10 seconds
  private Integer currentGameId = null;
  private String authToken = null;

  public WebSocketCommunicator(URI serverURI, ServerMessageObserver observer) throws Exception {
    this.observer = observer;
    this.gson = new Gson();
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    container.connectToServer(this, serverURI);
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    session.addMessageHandler(new MessageHandler.Whole<String>() {
      @Override
      public void onMessage(String message) {
        handleServerMessage(message);
      }
    });
  }

  private void handleServerMessage(String message) {
    try {
      ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
      observer.notify(serverMessage);
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    this.session = null;
    this.currentGameId = null;
    this.authToken = null;
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    System.err.println("WebSocket error: " + throwable.getMessage());
    // Notify observer of error
    observer.notify(new ErrorMessage("WebSocket error: " + throwable.getMessage()));
  }

  // Connect to a specific game
  public void connectToGame(Integer gameId, String authToken) throws IOException {
    this.currentGameId = gameId;
    this.authToken = authToken;

    UserGameCommand connectCommand = new UserGameCommand(
            UserGameCommand.CommandType.CONNECT,
            authToken,
            gameId
    );
    sendCommand(connectCommand);
  }

  // Send a chess move
  public void sendMove(ChessMove move) throws IOException {
    if (currentGameId == null) {
      throw new IllegalStateException("Not connected to a game");
    }

    UserGameCommand moveCommand = new UserGameCommand(
            authToken,
            currentGameId,
            move
    );
    sendCommand(moveCommand);
  }

  // Leave the current game
  public void leaveGame() throws IOException {
    if (currentGameId == null) {
      return; // Already not in a game
    }

    UserGameCommand leaveCommand = new UserGameCommand(
            UserGameCommand.CommandType.LEAVE,
            authToken,
            currentGameId
    );
    sendCommand(leaveCommand);
    currentGameId = null;
  }

  // Resign from the current game
  public void resignGame() throws IOException {
    if (currentGameId == null) {
      throw new IllegalStateException("Not connected to a game");
    }

    UserGameCommand resignCommand = new UserGameCommand(
            UserGameCommand.CommandType.RESIGN,
            authToken,
            currentGameId
    );
    sendCommand(resignCommand);
  }

  // Generic command sender
  public void sendCommand(UserGameCommand command) throws IOException {
    if (session == null || !session.isOpen()) {
      throw new IOException("WebSocket connection is not open");
    }

    String jsonCommand = gson.toJson(command);
    try {
      session.getBasicRemote().sendText(jsonCommand);
    } catch (IOException e) {
      System.err.println("Error sending command: " + e.getMessage());
      throw e;
    }
  }

  public boolean connectBlocking() throws Exception {
    long startTime = System.currentTimeMillis();
    while (session == null && System.currentTimeMillis() - startTime < TIMEOUT_MS) {
      Thread.sleep(100);
    }
    return session != null;
  }

  public boolean isConnected() {
    return session != null && session.isOpen();
  }

  public void close() {
    if (session != null) {
      try {
        leaveGame(); // Attempt to leave current game gracefully
        session.close();
      } catch (IOException e) {
        System.err.println("Error during WebSocket closure: " + e.getMessage());
      }
    }
  }

  public void close(int code, String reason) {
    if (session != null) {
      try {
        leaveGame(); // Attempt to leave current game gracefully
        session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(code), reason));
      } catch (IOException e) {
        System.err.println("Error during WebSocket closure: " + e.getMessage());
      }
    }
  }
}
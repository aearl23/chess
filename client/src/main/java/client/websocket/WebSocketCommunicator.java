package client.websocket;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class WebSocketCommunicator extends WebSocketClient {
  private final ServerMessageObserver observer;
  private final Gson gson;

  public WebSocketCommunicator(URI serverURI, ServerMessageObserver observer) {
    super(serverURI);
    this.observer = observer;
    this.gson = new Gson();
  }

  @Override
  public void onOpen(ServerHandshake handshake) {
    // Connection opened successfully
  }

  @Override
  public void onMessage(String message) {
    // Deserialize and handle incoming server message
    try {
      ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
      observer.notify(serverMessage);
    } catch (Exception e) {
      System.err.println("Error processing message: " + e.getMessage());
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    // Connection closed
  }

  @Override
  public void onError(Exception ex) {
    // Handle error
    System.err.println("WebSocket error: " + ex.getMessage());
  }

  public void sendCommand(UserGameCommand command) {
    String jsonCommand = gson.toJson(command);
    send(jsonCommand);
  }

  // Implementation of connectBlocking with timeout
  public boolean connectBlocking() throws InterruptedException {
    // Try to connect with timeout
    boolean connected = super.connectBlocking(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    if (!connected) {
      throw new InterruptedException("Failed to connect to server within " + CONNECTION_TIMEOUT_SECONDS + " seconds");
    }
    return connected;
  }

  // Implementation of close with proper cleanup
  @Override
  public void close() {
    try {
      // Close the connection with normal closure status
      super.close();
    } catch (Exception e) {
      System.err.println("Error during WebSocket closure: " + e.getMessage());
    }
  }

  // Additional close method with custom code and reason
  public void close(int code, String reason) {
    try {
      super.close(code, reason);
    } catch (Exception e) {
      System.err.println("Error during WebSocket closure: " + e.getMessage());
    }
  }
}
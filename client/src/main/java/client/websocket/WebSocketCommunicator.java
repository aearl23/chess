package client.websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

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
    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
    observer.notify(serverMessage);
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
}
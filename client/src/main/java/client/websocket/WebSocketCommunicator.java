package client.websocket;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;
import javax.websocket.*;
import java.net.URI;
import java.io.IOException;

@ClientEndpoint
public class WebSocketCommunicator extends Endpoint{
  private final ServerMessageObserver observer;
  private final Gson gson;
  private Session session = null;
  private static final long TIMEOUT_MS = 10000; // 10 seconds

  public WebSocketCommunicator(URI serverURI, ServerMessageObserver observer) throws Exception {
    this.observer = observer;
    this.gson = new Gson();
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    container.connectToServer(this, serverURI);
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    // Connection opened successfully
  }

  @OnMessage
  public void onMessage(String message, Session session) {
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
    // Connection closed
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    System.err.println("WebSocket error: " + throwable.getMessage());
  }

  public void sendCommand(UserGameCommand command) throws IOException {
    if (session != null) {
      String jsonCommand = gson.toJson(command);
      session.getBasicRemote().sendText(jsonCommand);
    }
  }

  public boolean connectBlocking() throws Exception {
    long startTime = System.currentTimeMillis();
    while (session == null && System.currentTimeMillis() - startTime < TIMEOUT_MS) {
      Thread.sleep(100);
    }
    return session != null;
  }

  public void close() {
    if (session != null) {
      try {
        session.close();
      } catch (IOException e) {
        System.err.println("Error during WebSocket closure: " + e.getMessage());
      }
    }
  }

  public void close(int code, String reason) {
    if (session != null) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(code), reason));
      } catch (IOException e) {
        System.err.println("Error during WebSocket closure: " + e.getMessage());
      }
    }
  }
}
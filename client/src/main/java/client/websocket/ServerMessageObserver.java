package client.websocket;

public interface ServerMessageObserver {
  void notify(ServerMessage message);
}


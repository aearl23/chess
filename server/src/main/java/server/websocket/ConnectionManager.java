package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionManager {
  public record Connection(String username, Session session, Integer gameId, ChessGame.TeamColor playerColor) {
  }

  private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, ArrayList<String>> gameConnections = new ConcurrentHashMap<>();
  private final Gson gson = new Gson();


  public void add(Integer gameId, String username, Session session, ChessGame.TeamColor playerColor) {
    if (gameId == null || username == null || session == null) {
      throw new IllegalArgumentException("Required parameters cannot be null");
    }

    // Remove any existing connection for this username
    remove(username);

    Connection connection = new Connection(username, session, gameId, playerColor);
    connections.put(username, connection);

    gameConnections.computeIfAbsent(gameId, k -> new ArrayList<>()).add(username);
  }

  public void remove(String username) {
    Connection connection = connections.get(username);
    if (connection != null) {
      ArrayList<String> gameUsers = gameConnections.get(connection.gameId());
      if (gameUsers != null) {
        gameUsers.remove(username);
        // Clean up empty game entries
        if (gameUsers.isEmpty()) {
          gameConnections.remove(connection.gameId());
        }
      }
      connections.remove(username);

      try {
        if (connection.session().isOpen()) {
          connection.session().close();
        }
      } catch (Exception e) {
        System.err.println("Error closing session: " + e.getMessage());
      }
    }
  }

  public Connection getConnection(Session session) {
    if (session == null) {
      return null;
    }
    return connections.values().stream()
            .filter(conn -> conn.session().equals(session))
            .findFirst()
            .orElse(null);
  }

  public void broadcast(Integer gameId, String excludeUsername, ServerMessage message) throws IOException {
    if (gameId == null || message == null) {
      throw new IllegalArgumentException("GameId and message cannot be null");
    }

    ArrayList<String> users = gameConnections.get(gameId);
    if (users != null) {
      List<String> disconnectedUsers = new ArrayList<>();

      for (String username : users) {
        if (!username.equals(excludeUsername)) {
          Connection connection = connections.get(username);
          if (connection != null && connection.session().isOpen()) {
            try {
              connection.session().getRemote().sendString(gson.toJson(message));
            } catch (IOException e) {
              System.err.println("Error sending message to " + username + ": " + e.getMessage());
              disconnectedUsers.add(username);
            }
          } else {
            disconnectedUsers.add(username);
          }
        }
      }

      // Clean up disconnected users
      for (String username : disconnectedUsers) {
        remove(username);
      }
    }
  }

  public void broadcastToGame(Integer gameId, ServerMessage message) throws IOException {
    if (gameId == null || message == null) {
      throw new IllegalArgumentException("GameId and message cannot be null");
    }

    ArrayList<String> users=gameConnections.get(gameId);
    if (users != null) {
      for (String username : users) {
        Connection connection=connections.get(username);
        if (connection != null && connection.session().isOpen()) {
          connection.session().getRemote().sendString(gson.toJson(message));
        }
      }
    }
  }

  public List<String> getGameParticipants(Integer gameId) {
    ArrayList<String> participants = gameConnections.get(gameId);
    return participants != null ? new ArrayList<>(participants) : new ArrayList<>();
  }

  public boolean isConnected(String username) {
    Connection connection = connections.get(username);
    return connection != null && connection.session().isOpen();
  }

  public void removeGame(Integer gameId) {
    ArrayList<String> users = gameConnections.get(gameId);
    if (users != null) {
      // Create a copy to avoid concurrent modification
      new ArrayList<>(users).forEach(this::remove);
      gameConnections.remove(gameId);
    }
  }

  public void cleanupInactiveSessions() {
    List<String> inactiveUsers = new ArrayList<>();

    connections.forEach((username, connection) -> {
      if (!connection.session().isOpen()) {
        inactiveUsers.add(username);
      }
    });

    inactiveUsers.forEach(this::remove);
  }
}
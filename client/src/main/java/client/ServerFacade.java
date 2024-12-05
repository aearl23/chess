package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;
import chess.Game;
import websocket.messages.ServerMessage;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import ui.ChessClient;
import client.websocket.WebSocketCommunicator;
import client.websocket.ServerMessageObserver;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

public class ServerFacade implements ServerMessageObserver{
  private final String serverUrl;
  private final Gson gson;

  private WebSocketCommunicator webSocketCommunicator;
  private final ChessClient chessClient;

  public ServerFacade(int port, ChessClient chessClient){
    serverUrl = "http://localhost:" + port;
    this.chessClient=chessClient;
    gson = new Gson();
  }

  public ServerFacade(String url, ChessClient chessClient) {
    serverUrl = url;
    this.chessClient=chessClient;
    gson = new Gson();

  }
  //Pre-login operations: Help, quit, login, register

  public AuthData register(String username, String password, String email) throws Exception{
    if (username == null || username.trim().isEmpty()) {
      throw new Exception("Error: Username cannot be empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new Exception("Error: Password cannot be empty");
    }
    if (email == null || email.trim().isEmpty()) {
      throw new Exception("Error: Email cannot be empty");
    }

    var user = new UserData(username, password, email);
    return makeRequest("POST", "/user", user, AuthData.class, null);
  }

  public AuthData login(String username, String password) throws Exception{
    if (username == null || username.trim().isEmpty()) {
      throw new Exception("Error: Username cannot be empty");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new Exception("Error: Password cannot be empty");
    }
    var user = new UserData(username, password, null);
    return makeRequest("POST", "/session", user, AuthData.class, null);
  }

  //Post-login operations: Help, logout, Create Game, List Games, Play Game, Observe Game

  public void logout(String authToken) throws Exception{
    makeRequest("DELETE", "/session", null, null, authToken);
  }

  public Collection<GameData> listGames(String authToken) throws Exception {
    record ListGamesResponse(Collection<GameData> games) {}
    var response = makeRequest("GET", "/game", null, ListGamesResponse.class, authToken);
    if (response == null) {
      throw new Exception("Error: Failed to get games list");
    }
    return response.games();
  }

  public GameData createGame(String gameName, String authToken) throws Exception {
    record CreateGameRequest(String gameName) {}
    var request = new CreateGameRequest(gameName);
    return makeRequest("POST", "/game", request, GameData.class, authToken);
  }

  public void joinGame(String playerColor, int gameID, String authToken) throws Exception {
    record JoinGameRequest(String playerColor, int gameID) {}
    var request = new JoinGameRequest(playerColor, gameID);
    makeRequest("PUT", "/game", request, null, authToken);
  }

  public void clear() throws Exception {
    makeRequest("DELETE", "/db", null, null, null);
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
    try {
      HttpURLConnection http = setupConnection(method, path, authToken);
      sendRequestBody(http, request);
      return handleResponse(http, responseClass);
    } catch (Exception ex) {
      throw new Exception("Error: " + ex.getMessage());
    }
  }

  private HttpURLConnection setupConnection(String method, String path, String authToken) throws Exception {
    URL url = new URI(serverUrl + path).toURL();
    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    http.setRequestMethod(method);
    http.setDoOutput(true);

    // Set headers
    http.addRequestProperty("Content-Type", "application/json");
    if (authToken != null) {
      http.addRequestProperty("Authorization", authToken);
    }

    return http;
  }

  private void sendRequestBody(HttpURLConnection http, Object request) throws Exception {
    if (request != null) {
      try (OutputStream reqBody = http.getOutputStream()) {
        String reqData = gson.toJson(request);
        reqBody.write(reqData.getBytes());
      }
    }
    http.connect();
  }

  private <T> T handleResponse(HttpURLConnection http, Class<T> responseClass) throws Exception {
    int statusCode = http.getResponseCode();

    return switch (statusCode) {
      case HttpURLConnection.HTTP_OK -> handleSuccessResponse(http, responseClass);
      case HttpURLConnection.HTTP_BAD_REQUEST -> throw new Exception("Error: Invalid request");
      case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new Exception("Error: Unauthorized");
      case HttpURLConnection.HTTP_FORBIDDEN -> throw new Exception("Error: Already taken");
      default -> throw new Exception("Error: " + readError(http));
    };
  }

  private <T> T handleSuccessResponse(HttpURLConnection http, Class<T> responseClass) throws Exception {
    if (responseClass != null) {
      try (InputStream respBody = http.getInputStream()) {
        InputStreamReader reader = new InputStreamReader(respBody);
        return gson.fromJson(reader, responseClass);
      }
    }
    return null;
  }

  private String readError(HttpURLConnection http) throws IOException {
    try (InputStream errorStream = http.getErrorStream()) {
      if (errorStream != null) {
        InputStreamReader reader = new InputStreamReader(errorStream);
        return gson.fromJson(reader, String.class);
      }
      return "Unknown error occurred";
    }
  }

  public ServerFacade(String url, ChessClient chessClient) {
    serverUrl = url;
    gson = new Gson();
    this.chessClient = chessClient;
  }

  public ServerFacade(int port, ChessClient chessClient) {
    serverUrl = "http://localhost:" + port;
    gson = new Gson();
    this.chessClient = chessClient;
  }

  // New WebSocket methods
  public void connectToGame(int gameID, String authToken) throws Exception {
    // First establish WebSocket connection
    String wsUrl = serverUrl.replace("http", "ws") + "/connect";
    URI uri = new URI(wsUrl);
    webSocketCommunicator = new WebSocketCommunicator(uri, this);
    webSocketCommunicator.connectBlocking();

    // Then send CONNECT command
    UserGameCommand connectCommand = new UserGameCommand();
    connectCommand.commandType = UserGameCommand.CommandType.CONNECT;
    connectCommand.gameID = gameID;
    connectCommand.authToken = authToken;
    webSocketCommunicator.sendCommand(connectCommand);
  }

  public void makeMove(int gameID, String authToken, chess.ChessMove move) throws Exception {
    if (webSocketCommunicator == null) {
      throw new Exception("Error: Not connected to game");
    }

    var moveCommand = new MakeMoveCommand();
    moveCommand.commandType = UserGameCommand.CommandType.MAKE_MOVE;
    moveCommand.gameID = gameID;
    moveCommand.authToken = authToken;
    moveCommand.move = move;
    webSocketCommunicator.sendCommand(moveCommand);
  }

  public void leaveGame(int gameID, String authToken) throws Exception {
    if (webSocketCommunicator == null) {
      throw new Exception("Error: Not connected to game");
    }

    var leaveCommand = new UserGameCommand();
    leaveCommand.commandType = UserGameCommand.CommandType.LEAVE;
    leaveCommand.gameID = gameID;
    leaveCommand.authToken = authToken;
    webSocketCommunicator.sendCommand(leaveCommand);

    // Close WebSocket connection
    webSocketCommunicator.close();
    webSocketCommunicator = null;
  }

  public void resignGame(int gameID, String authToken) throws Exception {
    if (webSocketCommunicator == null) {
      throw new Exception("Error: Not connected to game");
    }

    var resignCommand = new UserGameCommand();
    resignCommand.commandType = UserGameCommand.CommandType.RESIGN;
    resignCommand.gameID = gameID;
    resignCommand.authToken = authToken;
    webSocketCommunicator.sendCommand(resignCommand);
  }

  // Implementation of ServerMessageObserver interface
  @Override
  public void notify(ServerMessage message) {
    try {
      switch (message.getServerMessageType()) {
        case LOAD_GAME -> {
          LoadGameMessage loadMessage = (LoadGameMessage) message;
          chessClient.updateGameDisplay(loadMessage.getGame());
        }
        case ERROR -> {
          ErrorMessage errorMessage = (ErrorMessage) message;
          chessClient.displayError(errorMessage.getErrorMessage());
        }
        case NOTIFICATION -> {
          NotificationMessage notificationMessage = (NotificationMessage) message;
          chessClient.displayNotification(notificationMessage.getMessage());
        }
      }
    } catch (Exception e) {
      chessClient.displayError("Error processing server message: " + e.getMessage());
    }
  }

}


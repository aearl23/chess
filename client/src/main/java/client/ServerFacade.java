package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

public class ServerFacade {
  private final String serverUrl;
  private final Gson gson;

  public ServerFacade(int port){
    serverUrl = "http://localhost:" + port;
    gson = new Gson();
  }

  public ServerFacade(String url) {
    serverUrl = url;
    gson = new Gson();

  }
  //Pre-login operations: Help, quit, login, register

  public AuthData register(String username, String password, String email) throws Exception{
    var user = new UserData(username, password, email);
    return makeRequest("POST", "/user", user, AuthData.class, null);
  }

  public AuthData login(String username, String password) throws Exception{
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
      URL url = new URI(serverUrl + path).toURL();
      HttpURLConnection http = (HttpURLConnection) url.openConnection();
      http.setRequestMethod(method);
      http.setDoOutput(true);

      // Set headers
      http.addRequestProperty("Content-Type", "application/json");
      if (authToken != null) {
        http.addRequestProperty("Authorization", authToken);
      }

      // Send request body if it exists
      if (request != null) {
        try (OutputStream reqBody = http.getOutputStream()) {
          String reqData = gson.toJson(request);
          reqBody.write(reqData.getBytes());
        }
      }

      http.connect();
      int statusCode = http.getResponseCode();

      // Handle different status codes appropriately
      switch (statusCode) {
        case HttpURLConnection.HTTP_OK -> {
          if (responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
              InputStreamReader reader = new InputStreamReader(respBody);
              return gson.fromJson(reader, responseClass);
            }
          }
          return null;
        }
        case HttpURLConnection.HTTP_BAD_REQUEST -> throw new Exception("Error: Invalid request");
        case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new Exception("Error: Unauthorized");
        case HttpURLConnection.HTTP_FORBIDDEN -> throw new Exception("Error: Already taken");
        default -> throw new Exception("Error: " + readError(http));
      }
    } catch (Exception ex) {
      throw new Exception("Error: " + ex.getMessage());
    }
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
}


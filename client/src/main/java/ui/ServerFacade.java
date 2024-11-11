package ui;

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

  public ServerFacade(String url){
    serverUrl = url;
    gson = new Gson();
  }

  //Pre-login operations: Help, quit, login, register

  public AuthData register(UserData user) throws Exception{
    return makeRequest("POST", "/user", user, AuthData.class, null);
  }

  public AuthData login(UserData user) throws Exception{
    return makeRequest("POST", "/session", user, AuthData.class, null);
  }

  //Post login operations: Help, logout, Create Game, List Games, Play Game, Observe Game

  public void logout(String authToken) throws Exception{
    makeRequest("DELETE", "/session", null, null, authToken);
  }

  public Collection<GameData> listGames(String authToken) throws Exception {
    record ListGamesResponse(Collection<GameData> games) {}
    var response = makeRequest("GET", "/game", null, ListGamesResponse.class, authToken);
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
          reqBody.write(gson.toJson(request).getBytes());
        }
      }

      // Handle response
      http.connect();
      var status = http.getResponseCode();
      if (status != 200) {
        throw new Exception("Error: " + readError(http));
      }

      // Return response if expected
      if (responseClass != null) {
        try (InputStream respBody = http.getInputStream()) {
          InputStreamReader reader = new InputStreamReader(respBody);
          return gson.fromJson(reader, responseClass);
        }
      }
      return null;
    } catch (Exception ex) {
      throw new Exception("Error: " + ex.getMessage());
    }
  }

  public void clear() throws Exception {
    makeRequest("DELETE", "/db", null, null, null);
  }


  private String readError(HttpURLConnection http) throws IOException {
    var errorStream = http.getErrorStream();
    if (errorStream != null) {
      try (errorStream) {
        InputStreamReader reader = new InputStreamReader(errorStream);
        return gson.fromJson(reader, String.class);
      }
    }
    return "Unknown error occurred";
  }
}


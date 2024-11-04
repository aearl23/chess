package dataaccess;


import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlDataAccess implements DataAccess {

  private final Gson gson = new Gson();

  @Override
  public void clear() throws DataAccessException {
    try (var conn=DatabaseManager.getConnection()) {
      try (var statement=conn.prepareStatement("DELETE FROM auth_tokens")) {
        statement.executeUpdate();
      }
      try (var statement2 = conn.prepareStatement("DELETE FROM games")) {
        statement2.executeUpdate();
      }
      try (var statement3 = conn.prepareStatement("DELETE FROM users")){
        statement3.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public void createUser(UserData user) throws InvalidUsernameException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
        statement.setString(1, user.username());
        statement.setString(2, user.password());
        statement.setString(3, user.email());
        statement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      throw new InvalidUsernameException("Username already exists");
    }
  }

  @Override
  public UserData getUser(String username) throws DatabaseException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "SELECT username, password, email FROM users WHERE username = ?")){
        statement.setString(1, username);
        try (var resultsSet = statement.executeQuery()){
          if (resultsSet.next()) {
            return new UserData(
                    resultsSet.getString("username"),
                    resultsSet.getString("password"),
                    resultsSet.getString("email")
            );
          }
          return null;
        }
      }
    } catch (SQLException e) {
        throw new DatabaseException("Error accessing database: " + e.getMessage());
    } catch (DataAccessException e) {
        throw new DatabaseException("Database connection error: " + e.getMessage());
    }
  }

  @Override
  public int createGame(GameData game) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "INSERT INTO games (game_name, game_state) VALUES (?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, game.gameName());
        statement.setString(2, gson.toJson(game.game()));
        statement.executeUpdate();

        try (var generatedKeys = statement.getGeneratedKeys()){
          if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
          }
          throw new DataAccessException("Failed to create game");
        }
      }
    } catch (SQLException e) {
        throw new InvalidUsernameException(e.getMessage());
    }
  }


  @Override
  public GameData getGame(int gameID) throws BadRequestException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "SELECT game_name, game_state, white_username, black_username FROM games WHERE game_id = ?")) {
        statement.setInt(1, gameID);

        try (var resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            ChessGame game = gson.fromJson(resultSet.getString("game_state"), ChessGame.class);
            return new GameData(
                    gameID,
                    resultSet.getString("white_username"),
                    resultSet.getString("black_username"),
                    resultSet.getString("game_name"),
                    game
            );
          }
          throw new BadRequestException("Game not found");
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new BadRequestException("Error accessing game: " + e.getMessage());
    }
  }

  @Override
  public List<GameData> listGames() throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "SELECT game_id, game_name, game_state, white_username, black_username FROM games")) {

        try (var resultSet = statement.executeQuery()) {
          List<GameData> games = new ArrayList<>();
          while (resultSet.next()) {
            ChessGame game = gson.fromJson(resultSet.getString("game_state"), ChessGame.class);
            games.add(new GameData(
                    resultSet.getInt("game_id"),
                    resultSet.getString("white_username"),
                    resultSet.getString("black_username"),
                    resultSet.getString("game_name"),
                    game
            ));
          }
          return games;
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }
  }

  @Override
  public void updateGame(GameData game) throws BadRequestException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "UPDATE games SET game_state = ?, white_username = ?, black_username = ? WHERE game_id = ?")) {
        statement.setString(1, gson.toJson(game.game()));
        statement.setString(2, game.whiteUsername());
        statement.setString(3, game.blackUsername());
        statement.setInt(4, game.gameID());
        statement.executeUpdate();

        int rowsAffected = statement.executeUpdate();
        if (rowsAffected == 0) {
          throw new BadRequestException("Game not found");
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new BadRequestException("Error updating game: " + e.getMessage());
    }
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "INSERT INTO auth_tokens (auth_token, username) VALUES (?,?)")) {
        statement.setString(1, auth.authToken());
        statement.setString(2, auth.username());
        statement.executeUpdate();
      }
    } catch (SQLException e) {
        throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public AuthData getAuth(String authToken) throws UnauthorizedException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?")) {
        statement.setString(1, authToken);

        try (var resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return new AuthData(
                    resultSet.getString("auth_token"),
                    resultSet.getString("username")
            );
          }
          throw new UnauthorizedException("Invalid auth token");
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new UnauthorizedException("Error validating auth token: " + e.getMessage());
    }
  }

  @Override
  public void deleteAuth(String authToken) throws UnauthorizedException {
    try (var conn = DatabaseManager.getConnection()) {
      try (var statement = conn.prepareStatement(
              "DELETE FROM auth_tokens WHERE auth_token = ?")) {
        statement.setString(1, authToken);

        int rowsAffected = statement.executeUpdate();
        if (rowsAffected == 0) {
          throw new UnauthorizedException("Invalid auth token");
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new UnauthorizedException("Error deleting auth token: " + e.getMessage());
    }
  }
}

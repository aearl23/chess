package dataaccess;

import chess.ChessGame;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.mysql.cj.x.protobuf.MysqlxPrepare;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import com.google.gson.Gson;
import passoff.exception.ResponseParseException;

import javax.xml.crypto.Data;

public class MySqlDataAccess implements DataAccess {

  public final Gson gson = new Gson();

//  public MySqlDataAccess() throws DataAccessException {
//    configureDatabase();
//  }

  // Store user with hashed password
  public void storeUserPassword(String username, String password) throws DataAccessException {
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    String sql = "INSERT INTO Users (username, password_hash) VALUES (?, ?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, username);
      pstmt.setString(2, hashedPassword);
      pstmt.executeUpdate();

    } catch (SQLException e) {
      throw new DataAccessException("Error storing user password: ", e);
    }
  }

//  private void configureDatabase() throws DataAccessException {
//    DatabaseManager.createDatabase();
//    try (var conn = DatabaseManager.getConnection()) {
//      for (var statement : createStatements) {
//        try (var preparedStatement = conn.prepareStatement(statement)) {
//          preparedStatement.executeUpdate();
//        }
//      }
//    } catch (SQLException e) {
//      throw new DataAccessException("Error initializing database tables: " + e.getMessage());
//
//    }
//  }

    public void createUser(String username, String passwordHash) throws SQLException, DataAccessException {
      String sql = "INSERT INTO Users (username, password_hash) VALUES (?,?)";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)){
        pstmt.setString(1, username);
        pstmt.setString(2, passwordHash);
        pstmt.executeUpdate();
      }
    }
    public boolean verifyUser(String username, String cleartextpassword) throws SQLException, DataAccessException{
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, username);
          try (ResultSet rs = pstmt.executeQuery()) {
              if (rs.next()) {
                String hashPassword = rs.getString("password_hash");
                return BCrypt.checkpw(cleartextpassword, hashPassword);
              }
          }
        } catch (SQLException e) {
            throw new DataAccessException("Error verifying user: " + e.getMessage());
       }
       return false;
    }

    public void savegamestate(int userID, ChessGame game) throws DataAccessException, SQLException {
      //serialize game to json
      String gameStatejson = gson.toJson(game);

      String sql = "INSERT INTO Games (user_id, game_state) VALUES (?,?)";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)){
        pstmt.setInt(1, userID);
        pstmt.setString(2, gameStatejson);
        pstmt.executeUpdate();
      } catch (SQLException e) {
          throw new DataAccessException("Error saving game state: " + e.getMessage());
      }
    }

    public ChessGame loadgamestate(int gameID) throws DataAccessException, SQLException {
      String sql = "SELECT game_state FROM Games WHERE game_id = ?";
      try (Connection conn = DatabaseManager.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, gameID);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()){
            String gameStatejson = rs.getString("game_state");
            return gson.fromJson(gameStatejson, ChessGame.class); //Deserialization
          }
        }
      } catch (SQLException e) {
          throw new DataAccessException("Error loading game state: " + e.getMessage());
      }
      return null;
    }

//  static void initializeDatabase() throws DataAccessException {
//    createDatabase();
//    try (Connection conn = getConnection()) {
//      String usersTable = "CREATE TABLE IF NOT EXISTS Users ("
//              + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
//              + "username VARCHAR(50) UNIQUE NOT NULL, "
//              + "password_hash VARCHAR(60) NOT NULL)";
//
//      String gamesTable = "CREATE TABLE IF NOT EXISTS Games ("
//              + "game_id INT AUTO_INCREMENT PRIMARY KEY, "
//              + "user_id INT NOT NULL, "
//              + "game_state JSON NOT NULL, "
//              + "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)";
//
//      String movesTable = "CREATE TABLE IF NOT EXISTS Moves ("
//              + "move_id INT AUTO_INCREMENT PRIMARY KEY, "
//              + "game_id INT NOT NULL, "
//              + "move_order INT NOT NULL, "
//              + "move_description VARCHAR(100) NOT NULL, "
//              + "FOREIGN KEY (game_id) REFERENCES Games(game_id) ON DELETE CASCADE)";
//
//      try (Statement stmt = conn.createStatement()) {
//        stmt.executeUpdate(usersTable);
//        stmt.executeUpdate(gamesTable);
//        stmt.executeUpdate(movesTable);
//      }
//    } catch (SQLException e) {
//      throw new DataAccessException("Error initializing database tables: " + e.getMessage());
//    }
//  }




  // Clear method implementation to delete data from tables (based on assumption)
  @Override
  public void clear() throws DataAccessException {
    String sql = "DELETE FROM Users";

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement()) {

      stmt.executeUpdate(sql);

    } catch (SQLException e) {
      throw new DataAccessException("Error clearing user data", e);
    }
  }
}

package dataaccess;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlDataAccess implements DataAccess {


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
        throw new DataAccessException("Error storing user password: " + e.getMessage());
      }
    }

    @Override
    public void createUser(String username, String passwordHash) throws SQLException, DataAccessException {
      String sql = "INSERT INTO Users (username, password_hash) VALUES (?,?)";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)){
        pstmt.setString(1, username);
        pstmt.setString(2, passwordHash);
        pstmt.executeUpdate();
      }
    }
    @Override
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

  //Add saveGame, getgamestate,



}

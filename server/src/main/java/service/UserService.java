package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

public class UserService {
  private final DataAccess dataAccess;

  public UserService(DataAccess dataAccess) {

    this.dataAccess=dataAccess;
  }

  public AuthData register(UserData user) throws DataAccessException, DatabaseException {
    //check if user already exists
    try {
      UserData existingUser = dataAccess.getUser(user.username());
      if (existingUser != null) {
        throw new DatabaseException("Error: username already taken");
      }
      //create user
      dataAccess.createUser(user);
      //generate authToken
      return createAuthToken(user.username());
    } catch (DataAccessException e) {
      throw new DatabaseException("Error registering user: " + e.getMessage());
    }
  }

  public AuthData login(UserData loginData) throws DataAccessException, InvalidUsernameException, WrongPasswordException, DatabaseException {
    try {
      if (loginData == null || loginData.username() == null || loginData.password() == null) {
        throw new DatabaseException("Error: missing credentials");
      }

      UserData storedUser = dataAccess.getUser(loginData.username());

      //check if user exists
      if (storedUser == null) {
        throw new InvalidUsernameException("Error: invalid username");
      }

      if (!BCrypt.checkpw(loginData.password(), storedUser.password())) {
        throw new WrongPasswordException("Error: wrong password");
      }

      String authToken=UUID.randomUUID().toString();
      AuthData authData=new AuthData(authToken, loginData.username());
      dataAccess.createAuth(authData);
      return authData;

    }  catch (Exception e) {
       throw e;
    }
  }

  public void logout(String authToken) throws UnauthorizedException {
    try {
      if (dataAccess.getAuth(authToken) == null) {
        throw new UnauthorizedException("Error: unauthorized");
      }
      dataAccess.deleteAuth(authToken);
    } catch (Exception e) {
      throw e;
    }
  }

  private AuthData createAuthToken(String username) throws DatabaseException {
    try {
      // Generate an auth token
      String authToken=UUID.randomUUID().toString();
      AuthData authData=new AuthData(authToken, username);

      // Store the auth token in the database
      dataAccess.createAuth(authData);

      // Return the auth data
      return authData;

    } catch (Exception e) {
      // Catch any database-related exceptions during auth token creation
      throw new DatabaseException("Error while creating auth token");
    }
  }
}

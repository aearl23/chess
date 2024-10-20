package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

import javax.xml.crypto.Data;
import java.util.UUID;

public class UserService {
  private final DataAccess dataAccess;

  public UserService(DataAccess dataAccess){
    this.dataAccess = dataAccess;
  }
  public AuthData register(UserData user) throws DataAccessException {
    //check if user already exists
    if(dataAccess.getUser(user.username()) != null){
        throw new DataAccessException("Error: username already taken");
      }
      //create user
      dataAccess.createUser(user);

      //generate authToken
      return createAuthToken(user.username());
  }
  public AuthData login(String username, String password) throws DataAccessException {
    UserData user = dataAccess.getUser(username);
    if (user == null || !user.password().equals(password)){
      throw new DataAccessException("Error: unauthorized");
    }
    return createAuthToken(username);
  }
  public void logout(String authToken) throws DataAccessException{
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    dataAccess.deleteAuth(authToken);
  }

  private AuthData createAuthToken(String username) throws DataAccessException{
    //generate authToken
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, username);
    dataAccess.createAuth(authData);
    return authData;
  }
}

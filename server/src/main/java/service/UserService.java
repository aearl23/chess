package service;

import dataaccess.DataAccess;
import dataaccess.UnauthorizedException;
import model.UserData;
import model.AuthData;

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
  public AuthData login(UserData loginData) throws DataAccessException {
    if (loginData == null || loginData.username() == null || loginData.password() == null){
      throw new DataAccessException("Error: missing credentials");
    }
    UserData storedUser = dataAccess.getUser(loginData.username());

    //check if user exists
    if (storedUser == null) {
      throw new DataAccessException("Error: invalid username");
    }

    if(!storedUser.password().equals(loginData.password())){
      throw new DataAccessException("Error: wrong password");
    }
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, loginData.username());
    dataAccess.createAuth(authData);
    return authData;
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

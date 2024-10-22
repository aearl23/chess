package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

import java.nio.file.attribute.UserPrincipal;
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
  public AuthData login(UserData loginRequest) throws DataAccessException {
    UserData user = dataAccess.getUser(loginRequest.username());
    //check if user exists and if password matches
    if (user == null || !user.password().equals(loginRequest.password())){
      throw new DataAccessException("Error: unauthorized");
    }
    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(authToken, user.username());
    return authData;
  }
  public void logout(String authToken) throws DataAccessException{
    if (dataAccess.getAuth(authToken) == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    dataAccess.deleteAuth(authToken);
  }
}

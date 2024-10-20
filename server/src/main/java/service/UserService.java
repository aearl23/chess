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
      if(dataAccess.getUser(user.username()) != null){
        throw new DataAccessException("Error: username already taken");
      }
      dataAccess.createUser(user);

      String authToken = UUID.randomUUID().toString();
      AuthData authData = new AuthData(authToken, user.username());
      dataAccess.createAuth(authData);

      return authData;
  }
  public AuthData login(UserData user) {}
  public void logout(AuthData auth) {}
}

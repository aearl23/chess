package dataaccess;

import model.GameData;
import model.AuthData;
import model.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess{

  private final Map<String, UserData> users = new HashMap<>();
  private final Map<String, GameData> games = new HashMap<>();
  private final Map<String, AuthData> auths = new HashMap<>();
  private int nextGameID = 1;

  //implement all methods specified by DataAccess Interface
  @Override
  public void clear(){
    users.clear();
    games.clear();
    auths.clear();
    nextGameID = 1;
  }

  @Override
  public void createUser(UserData user) throws DataAccessException{
      if (users.containsKey(user.username())) {
        throw new DataAccessException("User already exists");
      }
      users.put(user.username(), user);
  }

  @Override
  public UserData getUser(String username) throws DataAccessException {
      return users.get(username);
  }


}


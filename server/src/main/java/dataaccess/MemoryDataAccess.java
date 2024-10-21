package dataaccess;

import model.GameData;
import model.AuthData;
import model.UserData;

import javax.xml.crypto.Data;
import java.util.*;

public class MemoryDataAccess implements DataAccess{

  private final Map<String, UserData> users = new HashMap<>();
  private final Map<Integer, GameData> games = new HashMap<>();
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

  @Override
  public int createGame(GameData game) throws DataAccessException {
    int gameID = nextGameID++;
    games.put(gameID, new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
    return gameID;
  }

  @Override
  public GameData getGame(int gameID) throws DataAccessException{
      return games.get(gameID);
  }

  @Override
  public List<GameData> listGames() throws DataAccessException {
    return new ArrayList<>(games.values());
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException{
    if (!games.containsKey(game.gameID())){
        throw new DataAccessException("Game doesn't exist");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException{
    auths.put(auth.authToken(),auth);
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException{
    return auths.get(authToken);
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException{
    auths.remove(authToken);
  }
}


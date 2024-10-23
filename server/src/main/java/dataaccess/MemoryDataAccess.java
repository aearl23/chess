package dataaccess;

import model.GameData;
import model.AuthData;
import model.UserData;

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
  public void createUser(UserData user) throws InvalidUsernameException{
      if (users.containsKey(user.username())) {
        throw new InvalidUsernameException("User already exists");
      }
      users.put(user.username(), user);
  }

  @Override
  public UserData getUser(String username) {
      return users.get(username);
  }

  @Override
  public int createGame(GameData game) throws DatabaseException {
    int gameID = nextGameID++;
    games.put(gameID, new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()));
    return gameID;
  }

  @Override
  public GameData getGame(int gameID) throws BadRequestException{
    GameData game = games.get(gameID);
    if (game == null) {
      throw new BadRequestException("Error: Game not found");
    }
    return games.get(gameID);
  }

  @Override
  public List<GameData> listGames() {
    return new ArrayList<>(games.values());
  }

  @Override
  public void updateGame(GameData game) throws BadRequestException{
    if (!games.containsKey(game.gameID())){
        throw new BadRequestException("Game doesn't exist");
    }
    games.put(game.gameID(), game);
  }

  @Override
  public void createAuth(AuthData auth) {
    auths.put(auth.authToken(),auth);
  }

  @Override
  public AuthData getAuth(String authToken) throws UnauthorizedException{
    AuthData auth = auths.get(authToken);
    if (auth == null) {
      throw new UnauthorizedException("Error: Unauthorized");
    }
    return auth;
  }

  @Override
  public void deleteAuth(String authToken) throws UnauthorizedException{
    if (auths.remove(authToken) == null) {
      throw new UnauthorizedException("Error: Auth token not found");
    }
  }
}


package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;

import java.util.List;

public interface DataAccess {
  void clear() throws DataAccessException;

  void createUser(UserData user) throws InvalidUsernameException;
  UserData getUser(String username) throws DatabaseException;

  int createGame(GameData game) throws DataAccessException;
  GameData getGame(int gameID) throws BadRequestException;
  List<GameData> listGames() throws DataAccessException;
  void updateGame(GameData game) throws BadRequestException;

  void createAuth(AuthData auth) throws DataAccessException;
  AuthData getAuth(String authToken) throws UnauthorizedException;
  void deleteAuth(String authToken) throws UnauthorizedException;
}

package dataaccess;

import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import chess.*;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class DBTests {
  private static DataAccess dataAccess;
  private static ChessGame initialGame;

  @BeforeAll
  public static void init() throws DataAccessException {
    dataAccess = new MySqlDataAccess();
    initialGame = new ChessGame();
    initialGame.getBoard().resetBoard();
  }

  @BeforeEach
  public void setup() throws DataAccessException {
    dataAccess.clear();
  }

  @Test
  @DisplayName("Positive createuser")
  public void createUserpos() throws InvalidUsernameException{
    UserData user = new UserData("testUser", "password123", "test@email.com");
    assertDoesNotThrow(() -> dataAccess.createUser(user));
  }

  @Test
  @DisplayName("Duplicate user")
  public void duplicateUser() throws InvalidUsernameException {
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);
    assertThrows(InvalidUsernameException.class, () -> dataAccess.createUser(user));
  }

  @Test
  @DisplayName("get user positive")
  public void getUserSuccess() throws InvalidUsernameException, DatabaseException {
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);

    UserData retrievedUser = dataAccess.getUser("testUser");
    assertEquals(user.username(), retrievedUser.username());
    assertEquals(user.email(), retrievedUser.email());
  }

  @Test
  @DisplayName("user doesn't exist")
  public void getUserNonexistentFails() throws DatabaseException{
    UserData retrievedUser = dataAccess.getUser("nonexistentUser");
    assertNull(retrievedUser);
  }

  @Test
  @DisplayName("Create game positive")
  public void createGameSuccess() throws DataAccessException {
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);
    assertTrue(gameId > 0);
  }

  @Test
  @DisplayName("get game positive")
  public void getGameSuccess() throws DataAccessException, BadRequestException {
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

    GameData retrievedGame = dataAccess.getGame(gameId);
    assertEquals(game.gameName(), retrievedGame.gameName());
  }

  @Test
  @DisplayName("Game nonexistent")
  public void getGameNonexistentFails() {
    assertThrows(BadRequestException.class,
            () -> dataAccess.getGame(99999));
  }

  @Test
  @DisplayName("Update game positive")
  public void updateGameSuccess() throws DataAccessException, BadRequestException {
    // Create initial game
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

    // Create users first
    UserData whiteUser = new UserData("white_player", "pass123", "white@email.com");
    UserData blackUser = new UserData("black_player", "pass123", "black@email.com");
    dataAccess.createUser(whiteUser);
    dataAccess.createUser(blackUser);

    // Update game with players
    GameData updatedGame = new GameData(gameId, "white_player", "black_player",
            "Test Game", initialGame);
    assertDoesNotThrow(() -> dataAccess.updateGame(updatedGame));

    // Verify update
    GameData retrievedGame = dataAccess.getGame(gameId);
    assertEquals("white_player", retrievedGame.whiteUsername());
    assertEquals("black_player", retrievedGame.blackUsername());
  }

  @Test
  @DisplayName("update game")
  public void updateGameWithMoves() throws DataAccessException, BadRequestException, InvalidMoveException {
    // Create initial game
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

    // Create users first
    UserData whiteUser = new UserData("white_player", "pass123", "white@email.com");
    UserData blackUser = new UserData("black_player", "pass123", "black@email.com");
    dataAccess.createUser(whiteUser);
    dataAccess.createUser(blackUser);

    // Make a move
    ChessGame chessGame = new ChessGame();
    chessGame.getBoard().resetBoard();
    ChessPosition fromPosition = new ChessPosition(2, 1);
    ChessPosition toPosition = new ChessPosition(4, 1);
    chessGame.makeMove(new ChessMove(fromPosition, toPosition, null));

    // Update game with new state
    GameData updatedGame = new GameData(gameId, "white_player", "black_player",
            "Test Game", chessGame);
    dataAccess.updateGame(updatedGame);

    // Verify move was persisted
    GameData retrievedGame = dataAccess.getGame(gameId);
    ChessGame retrievedChessGame = retrievedGame.game();
    assertNotNull(retrievedChessGame.getBoard().getPiece(toPosition));
  }

  @Test
  @DisplayName("list games Positive")
  public void listGamesSuccess() throws DataAccessException {
    // Create multiple games
    GameData game1 = new GameData(0, null, null, "Game 1", initialGame);
    GameData game2 = new GameData(0, null, null, "Game 2", initialGame);

    dataAccess.createGame(game1);
    dataAccess.createGame(game2);

    var games = dataAccess.listGames();
    assertEquals(2, games.size());
  }

  @Test
  @DisplayName("create auth positive")
  public void createAuthSuccess() throws DataAccessException {
    // First create a user since auth requires valid user
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);

    AuthData auth = new AuthData("testToken", "testUser");
    assertDoesNotThrow(() -> dataAccess.createAuth(auth));
  }

  @Test
  @DisplayName("get Auth Positive")
  public void getAuthSuccess() throws DataAccessException, UnauthorizedException {
    // First create a user
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);

    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);

    AuthData retrievedAuth = dataAccess.getAuth("testToken");
    assertEquals(auth.username(), retrievedAuth.username());
    assertEquals(auth.authToken(), retrievedAuth.authToken());
  }

  @Test
  @DisplayName("Invalid auth")
  public void getAuthInvalidFails() {
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.getAuth("invalidToken"));
  }

  @Test
  @DisplayName("Delete Auth Positive")
  public void deleteAuthSuccess() throws DataAccessException, UnauthorizedException {
    // First create a user
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);

    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);

    assertDoesNotThrow(() -> dataAccess.deleteAuth("testToken"));
    // Verify the auth token is deleted
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.getAuth("testToken"));
  }

  @Test
  @DisplayName("Delete Auth fails")
  public void deleteAuthNonexistentFails() {
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.deleteAuth("nonexistentToken"));
  }

  @Test
  @DisplayName("Clear Test")
  public void clearSuccess() {
    assertDoesNotThrow(() -> dataAccess.clear());
  }
}


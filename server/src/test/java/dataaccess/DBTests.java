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
  public void getUser_success() throws InvalidUsernameException, DatabaseException {
    UserData user = new UserData("testUser", "password123", "test@email.com");
    dataAccess.createUser(user);

    UserData retrievedUser = dataAccess.getUser("testUser");
    assertEquals(user.username(), retrievedUser.username());
    assertEquals(user.email(), retrievedUser.email());
  }

  @Test
  @DisplayName("user doesn't exist")
  public void getUser_nonexistent_fails() {
    assertThrows(DatabaseException.class,
            () -> dataAccess.getUser("nonexistentUser"));
  }

  @Test
  @DisplayName("Create game positive")
  public void createGame_success() throws DataAccessException {
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);
    assertTrue(gameId > 0);
  }

  @Test
  @DisplayName("get game positive")
  public void getGame_success() throws DataAccessException, BadRequestException {
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

    GameData retrievedGame = dataAccess.getGame(gameId);
    assertEquals(game.gameName(), retrievedGame.gameName());
  }

  @Test
  @DisplayName("Game nonexistent")
  public void getGame_nonexistent_fails() {
    assertThrows(BadRequestException.class,
            () -> dataAccess.getGame(99999));
  }

  @Test
  @DisplayName("Update game positive")
  public void updateGame_success() throws DataAccessException, BadRequestException {
    // Create initial game
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

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
  public void updateGame_withMoves() throws DataAccessException, BadRequestException, InvalidMoveException {
    // Create initial game
    GameData game = new GameData(0, null, null, "Test Game", initialGame);
    int gameId = dataAccess.createGame(game);

    // Make a move
    ChessGame chessGame = initialGame;
    ChessPosition fromPosition = new ChessPosition(2, 1); // e.g., pawn at e2
    ChessPosition toPosition = new ChessPosition(4, 1);   // move to e4
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
  public void listGames_success() throws DataAccessException {
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
  public void createAuth_success() throws DataAccessException {
    AuthData auth = new AuthData("testToken", "testUser");
    assertDoesNotThrow(() -> dataAccess.createAuth(auth));
  }

  @Test
  @DisplayName("get Auth Positive")
  public void getAuth_success() throws DataAccessException, UnauthorizedException {
    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);

    AuthData retrievedAuth = dataAccess.getAuth("testToken");
    assertEquals(auth.username(), retrievedAuth.username());
  }

  @Test
  @DisplayName("Invalid auth")
  public void getAuth_invalid_fails() {
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.getAuth("invalidToken"));
  }

  @Test
  @DisplayName("Delete Auth Positive")
  public void deleteAuth_success() throws DataAccessException, UnauthorizedException {
    AuthData auth = new AuthData("testToken", "testUser");
    dataAccess.createAuth(auth);

    assertDoesNotThrow(() -> dataAccess.deleteAuth("testToken"));
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.getAuth("testToken"));
  }

  @Test
  @DisplayName("Delete Auth fails")
  public void deleteAuth_nonexistent_fails() {
    assertThrows(UnauthorizedException.class,
            () -> dataAccess.deleteAuth("nonexistentToken"));
  }

  @Test
  @DisplayName("Clear Test")
  public void clear_success() {
    assertDoesNotThrow(() -> dataAccess.clear());
  }
}


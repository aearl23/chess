package service;

import dataaccess.*;
import model.UserData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GameServiceTest {
  private GameService gameService;
  private UserService userService;
  private DataAccess dataAccess;
  private String authToken;

  @BeforeEach
  public void setUp() throws DataAccessException {
    dataAccess = new MemoryDataAccess();
    gameService = new GameService(dataAccess);
    userService = new UserService(dataAccess);
    UserData userData = new UserData("testUser", "password", "test@example.com");
    authToken = userService.register(userData).authToken();
  }

  @Test
  @DisplayName("List Games Positive")
  public void testListGamesPositive() throws DataAccessException {
    gameService.createGame(authToken, "Game1");
    gameService.createGame(authToken, "Game2");
    List<GameData> games = gameService.listGames(authToken);
    assertEquals(2, games.size());
  }

  @Test
  @DisplayName("List Games Negative")
  public void testListGamesNegative() {
    assertThrows(UnauthorizedException.class, () -> gameService.listGames("invalidAuthToken"));
  }

  @Test
  @DisplayName("Create Game Positive")
  public void testCreateGamePositive() throws DataAccessException {
    int gameId = gameService.createGame(authToken, "NewGame");
    assertTrue(gameId > 0);
  }

  @Test
  @DisplayName("Create Game Negative")
  public void testCreateGameNegative() {
    assertThrows(UnauthorizedException.class, () -> gameService.createGame("invalidAuthToken", "NewGame"));
  }

  @Test
  @DisplayName("Join Game Positive")
  public void testJoinGamePositive() throws DataAccessException {
    int gameId = gameService.createGame(authToken, "JoinGame");
    assertDoesNotThrow(() -> gameService.joinGame(authToken, "WHITE", gameId));
  }

  @Test
  @DisplayName("Join Game Negative")
  public void testJoinGameNegative() throws DataAccessException {
    int gameId = gameService.createGame(authToken, "JoinGame");
    gameService.joinGame(authToken, "WHITE", gameId);
    assertThrows(GameAlreadyTakenException.class, () -> gameService.joinGame(authToken, "WHITE", gameId)); // Trying to join as WHITE again
  }
}

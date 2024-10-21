package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {
  private AdminService adminService;
  private UserService userService;
  private GameService gameService;
  private DataAccess dataAccess;

  @BeforeEach
  public void setup() {
    dataAccess = new MemoryDataAccess();
    adminService = new AdminService(dataAccess);
    userService = new UserService(dataAccess);
    gameService = new GameService(dataAccess);
  }

  @Test
  @DisplayName("Clear Positive Test")
  public void testclearpositive() throws DataAccessException{
      //Add data
      UserData  userdata = new UserData("testuser", "password", "testemail@test.com");
      String authToken = userService.register(userdata).authToken();
      gameService.createGame(authToken, "TestGame");

      //clear
      assertDoesNotThrow(() -> adminService.clearApplication());

      //verify
      assertThrows(DataAccessException.class, () -> userService.login("testUser", "password"));
      assertTrue(gameService.listGames(authToken).isEmpty());
  }
}

package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
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
  @DisplayName("RegisterAndLogin")
  public void registerandlogin() throws DataAccessException  {
    UserData registerData = new UserData("testUser", "testPass", "test@gmail.com");
    AuthData registerResult = userService.register(registerData);

    assertNotNull(registerResult);
    assertEquals("testUser", registerResult.username());

    // Login with the same user
    UserData loginData = new UserData("testUser", "testPass", null);
    AuthData loginResult = userService.login(loginData);

    assertNotNull(loginResult);
    assertEquals("testUser", loginResult.username());
    // Check that we got a new auth token
    assertNotEquals(registerResult.authToken(), loginResult.authToken());
  }

  @Test
  @DisplayName("Clear Positive Test")
  public void testclearpositive() throws DataAccessException{
      //Add data
      UserData  userdata = new UserData("testuser", "password", "testemail@test.com");
      userService.register(userdata);

      //clear
      dataAccess.clear();
      //Try to login - should fail
      UserData loginData = new UserData("testuser", "testPass", null);
      assertThrows(DataAccessException.class, () -> userService.login(loginData));
  }
}

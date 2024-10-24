package service;

import dataaccess.*;
import model.AuthData;
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
  @DisplayName("RegisterAndLogin")
  public void registerandlogin() throws InvalidUsernameException, WrongPasswordException, DatabaseException {
    try {
      UserData registerData=new UserData("testUser", "testPass", "test@gmail.com");
      AuthData registerResult=userService.register(registerData);

      assertNotNull(registerResult);
      assertEquals("testUser", registerResult.username());

      // Login with the same user
      UserData loginData=new UserData("testUser", "testPass", null);
      AuthData loginResult=userService.login(loginData);

      assertNotNull(loginResult);
      assertEquals("testUser", loginResult.username());
      // Check that we got a new auth token
      assertNotEquals(registerResult.authToken(), loginResult.authToken());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  @Test
  @DisplayName("Clear Positive Test")
  public void testclearpositive() throws DatabaseException, DataAccessException{
      //Add data
      UserData  userdata = new UserData("testuser", "password", "testemail@test.com");
      userService.register(userdata);

      //clear
      dataAccess.clear();
      //Try to log in  - should fail
      UserData loginData = new UserData("testuser", "testPass", null);
      assertThrows(InvalidUsernameException.class, () -> userService.login(loginData));
  }
}

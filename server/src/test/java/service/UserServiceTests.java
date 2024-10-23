package service;


import dataaccess.*;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
  private UserService userService;
  private DataAccess dataAccess;

  @BeforeEach
  public void setUp() {
    dataAccess = new MemoryDataAccess();
    userService = new UserService(dataAccess);
  }

  @Test
  @DisplayName("Register Positive")
  public void testRegisterPositive() throws DatabaseException {
    UserData userData = new UserData("testUser", "password", "test@example.com");
    AuthData authData = userService.register(userData);
    assertNotNull(authData);
    assertEquals("testUser", authData.username());
    assertNotNull(authData.authToken());
  }

  @Test
  @DisplayName("Register Negative")
  public void testRegisterNegative() {
    UserData userData = new UserData("testUser", "password", "test@example.com");
    assertThrows(DatabaseException.class, () -> {
      userService.register(userData);
      userService.register(userData); // Trying to register the same user again
    });
  }

  @Test
  @DisplayName("Login Positive")
  public void testLoginPositive() throws DatabaseException, WrongPasswordException, InvalidUsernameException {
    UserData userData = new UserData("testUser", "password", "test@gmail.com");
    userService.register(userData);
    UserData loginData = new UserData("testUser", "password", "test@gmail.com");
    AuthData result = userService.login(loginData);
    assertNotNull(result);
    assertEquals("testUser", result.username());
    assertNotNull(result.authToken());
  }

  @Test
  @DisplayName("Login Negative")
  public void testLoginNegative() {
    UserData userData = new UserData("testUser", "password", "test@example.com");
    assertThrows(DatabaseException.class, () -> {
      userService.register(userData);
      UserData wrongData = new UserData("testuser", "wrongpass", null);
      userService.login(wrongData);
    });
  }

  @Test
  @DisplayName("Logout Positive")
  public void testLogoutPositive() throws DatabaseException {
    UserData userData = new UserData("testUser", "password", "test@example.com");
    AuthData authData = userService.register(userData);
    assertDoesNotThrow(() -> userService.logout(authData.authToken()));
  }

  @Test
  @DisplayName("Logout Negative")
  public void testLogoutNegative() {
    assertThrows(DatabaseException.class, () -> userService.logout("invalidAuthToken"));
  }
}


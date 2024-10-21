package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import java.rmi.MarshalledObject;

import static org.junit.jupiter.api.Assertions.;


public class AdminServiceTest {
  private final AdminService adminService;
  private final UserService userService;
  private final GameService gameService;
  private final DataAccess dataAccess;

  @BeforeEach
  public void setup(){
    dataAccess = new MemoryDataAccess();
    adminService = new AdminService(dataAccess);
    userService = new UserService(dataAccess);
    gameService = new GameService(dataAccess);
  }

  @Test
  public void testclearpositive() throws DataAccessException{}
      //Add data
      UserData  userdata = new UserData("testuser", "password", "testemail@test.com");
      String authToken = userService.register(userdata).authToken();
      gameService.createGame(authToken, "TestGame");

      //clear
      assertDoesNotThrow(() -> adminService.clearApplication());

      //verify
      assertThrows(DataAccessException.class, () -> )
      assertTrue(gameService.listGames(authToken).isEmpty());
}

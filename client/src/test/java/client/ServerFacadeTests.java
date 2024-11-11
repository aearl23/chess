package client;

import jdk.jfr.Experimental;
import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import model.UserData;
import ui.ServerFacade;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "testPass";
    private static final String EMAIL = "test@email.com";


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }


    @Test
    @DisplayName("Register Positive")
    public void registerPositive() throws Exception {
        var user = new UserData(USERNAME, PASSWORD, EMAIL);
        AuthData auth = facade.register(user);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(USERNAME, auth.username());
    }

    @Test
    @DisplayName("Register Negative")
    public void registerNegative() throws Exception {
        var user = new UserData(null, PASSWORD, EMAIL);
        assertThrows(Exception.class, () -> facade.register(user));
    }

    @Test
    @DisplayName("login Positive")
    public void loginPositive() throws Exception {
        var registerUser = new UserData(USERNAME, PASSWORD, EMAIL);
        facade.register(registerUser);

        var loginUser = new UserData(USERNAME, PASSWORD, null);
        AuthData auth = facade.login(loginUser);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(USERNAME, auth.username());
    }

    @Test
    @DisplayName("login Negative")
    void loginNegative() throws Exception {
        var loginUser = new UserData(USERNAME, PASSWORD, null);
        assertThrows(Exception.class, () -> facade.login(loginUser));
    }


    @Test
    @DisplayName("logout Positive")
    void logoutPositive() throws Exception {
        // Register and login
        var user = new UserData(USERNAME, PASSWORD, EMAIL);
        AuthData auth = facade.register(user);

        // Logout
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    @DisplayName("logout Negative")
    void logoutNegative() {
        // Try to logout with invalid auth token
        assertThrows(Exception.class, () -> facade.logout("invalid_token"));
    }

    @Test
    void createGamePositive() throws Exception {
        // Register and get auth token
        var user = new UserData(USERNAME, PASSWORD, EMAIL);
        AuthData auth = facade.register(user);

        // Create game
        assertDoesNotThrow(() -> facade.createGame("testGame", auth.authToken()));
    }

    @Test
    void listGamesPositive() throws Exception {
        // Register and get auth token
        var user = new UserData(USERNAME, PASSWORD, EMAIL);
        AuthData auth = facade.register(user);

        // Create a game
        facade.createGame("testGame", auth.authToken());

        // List games
        var games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(1, games.size());
    }
}


package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import model.UserData;
import model.GameData;

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
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(USERNAME, auth.username());
    }

    @Test
    @DisplayName("Register Negative")
    public void registerNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.register(null, PASSWORD, EMAIL));
    }

    @Test
    @DisplayName("login Positive")
    public void loginPositive() throws Exception {
        facade.register(USERNAME, PASSWORD, EMAIL);

        AuthData auth = facade.login(USERNAME, PASSWORD);
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals(USERNAME, auth.username());
    }

    @Test
    @DisplayName("login Negative")
    void loginNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.login(USERNAME, null));
    }


    @Test
    @DisplayName("logout Positive")
    void logoutPositive() throws Exception {
        // Register and login
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);

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
    @DisplayName("CreateGame Positive")
    void createGamePositive() throws Exception {
        // Register and get auth token
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);

        // Create game
        assertDoesNotThrow(() -> facade.createGame("testGame", auth.authToken()));
    }

    @Test
    @DisplayName("CreateGame Negative")
    void createGameNegative() {
        assertThrows(Exception.class, () -> facade.createGame("testGame", "invalid_token"));
    }

    @Test
    @DisplayName("listGamesPositive")
    void listGamesPositive() throws Exception {
        // Register and get auth token
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);

        // Create a game
        facade.createGame("testGame", auth.authToken());

        // List games
        var games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(1, games.size());
    }

    @Test
    @DisplayName("ListGames Negative")
    void listGamesNegative() {
        assertThrows(Exception.class, () -> facade.listGames("invalid_token"));
    }

    @Test
    @DisplayName("JoinGame Positive")
    void joinGamePositive() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        GameData game = facade.createGame("testGame", auth.authToken());
        assertDoesNotThrow(() -> facade.joinGame("WHITE", game.gameID(), auth.authToken()));
    }

    @Test
    @DisplayName("JoinGame Negative")
    void joinGameNegative() {
        assertThrows(Exception.class, () -> facade.joinGame("WHITE", 999, "invalid_token"));
    }

    @Test
    @DisplayName("Clear Positive")
    void clearPositive() {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    @DisplayName("Clear Negative")
    void clearNegative() throws Exception {
        AuthData auth = facade.register(USERNAME, PASSWORD, EMAIL);
        facade.createGame("testGame", auth.authToken());
        facade.clear();
        // Verify the database is cleared by trying to list games
        assertThrows(Exception.class, () -> facade.listGames(auth.authToken()));
    }
}


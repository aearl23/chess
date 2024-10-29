package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }



    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */

    // create the database and schema tables
    static void initializeDatabase() throws DataAccessException {
        createDatabase();
        try (Connection conn = getConnection()) {
            String usersTable = "CREATE TABLE IF NOT EXISTS Users ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password_hash VARCHAR(60) NOT NULL)";

            String gamesTable = "CREATE TABLE IF NOT EXISTS Games ("
                    + "game_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "game_state JSON NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)";

            String movesTable = "CREATE TABLE IF NOT EXISTS Moves ("
                    + "move_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "game_id INT NOT NULL, "
                    + "move_order INT NOT NULL, "
                    + "move_description VARCHAR(100) NOT NULL, "
                    + "FOREIGN KEY (game_id) REFERENCES Games(game_id) ON DELETE CASCADE)";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(usersTable);
                stmt.executeUpdate(gamesTable);
                stmt.executeUpdate(movesTable);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error initializing database tables: " + e.getMessage());
        }
    }

    static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}

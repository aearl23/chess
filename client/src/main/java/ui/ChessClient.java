package ui;

import chess.ChessBoard;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import client.ServerFacade;

public class ChessClient {
  private final ServerFacade server;
  private final Scanner scanner;
  private String authToken=null;
  private List<GameData> gamesList=new ArrayList<>();

  public ChessClient(String serverUrl) {
    server=new ServerFacade(serverUrl);
    scanner=new Scanner(System.in);
  }

  public void run() {
    System.out.println(EscapeSequences.ERASE_SCREEN + "♕ Welcome to 240 Chess ♕");
    preloginUI();
  }

  private void preloginUI() {
    boolean running=true;
    while (running && authToken == null) {
      displayPreloginMenu();
      String command=scanner.nextLine().toLowerCase();

      try {
        switch (command) {
          case "help" -> displayHelp();
          case "quit" -> {
            running=false;
            System.out.println("Thanks for playing!");
          }
          case "login" -> login();
          case "register" -> register();
          default -> System.out.println("Unknown command. Type 'help' for list of commands.");
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

    if (authToken != null) {
      postloginUI(); // Transition to post-login UI when logged in
    }
  }

  private void displayPreloginMenu() {
    System.out.println("""
                            
            Please enter a command:
              help - Display available commands
              quit - Exit the program
              login - Login to an existing account
              register - Create a new account
            """);
  }

  private void displayHelp() {
    System.out.println("""
            Available commands:
              help - Display this help message
              quit - Exit the program
              login - Login to an existing account
              register - Create a new account
            """);
  }

  private void login() throws Exception {
    System.out.print("Username: ");
    String username=scanner.nextLine();

    System.out.print("Password: ");
    String password=scanner.nextLine();

    // Call server to login
    AuthData result=server.login(username, password);
    authToken=result.authToken();
    System.out.println("Logged in as " + result.username());
  }

  private void register() throws Exception {
    System.out.print("Username: ");
    String username=scanner.nextLine();

    System.out.print("Password: ");
    String password=scanner.nextLine();

    System.out.print("Email: ");
    String email=scanner.nextLine();

    // Call server to register
    AuthData result=server.register(username, password, email);
    authToken=result.authToken();
    System.out.println("Registered and logged in as " + result.username());
  }

  private void postloginUI() {
    // This will be implemented post-login functionality
    boolean running=true;
    while (running && authToken != null) {
      displayPostloginMenu();
      String command=scanner.nextLine().toLowerCase();

      try {
        switch (command) {
          case "help" -> displayPostloginHelp();
          case "logout" -> {
            logout();
            running=false;
          }
          case "create" -> createGame();
          case "list" -> listGames();
          case "join" -> joinGame();
          case "observe" -> observeGame();
          default -> System.out.println("Unknown command. Type 'help' for list of commands.");
        }
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }

    if (authToken == null) {
      preloginUI(); // Return to prelogin UI after logout
    }
  }

  private void displayPostloginMenu() {
    System.out.println("""
                            
            Please enter a command:
              help - Display available commands
              logout - Logout and return to main menu
              create - Create a new chess game
              list - List all chess games
              join - Join an existing chess game
              observe - Observe an existing chess game
            """);
  }

  private void displayPostloginHelp() {
    System.out.println("""
            Available commands:
              help - Display this help message
              logout - Logout and return to main menu
              create - Create a new chess game
              list - List all chess games
              join - Join an existing chess game as a player
              observe - Observe an existing chess game
            """);
  }

  private void logout() throws Exception {
    server.logout(authToken);
    System.out.println("Logged out successfully");
    authToken=null;
  }

  private void createGame() throws Exception {
    System.out.print("Enter name for new game: ");
    String gameName=scanner.nextLine();

    GameData game=server.createGame(gameName, authToken);
    System.out.println("Game '" + gameName + "' created successfully");

    displayGame(game);
  }

  private void listGames() throws Exception {
    gamesList=new ArrayList<>(server.listGames(authToken));

    if (gamesList.isEmpty()) {
      System.out.println("No games available");
      return;
    }

    System.out.println("\nAvailable Games:");
    for (int i=0; i < gamesList.size(); i++) {
      GameData game=gamesList.get(i);
      System.out.printf("%d. %s%n", (i + 1), formatGameInfo(game));
    }
  }

  private String formatGameInfo(GameData game) {
    StringBuilder info=new StringBuilder(game.gameName());
    info.append(" (");
    if (game.whiteUsername() != null) {
      info.append("White: ").append(game.whiteUsername());
    }
    if (game.whiteUsername() != null && game.blackUsername() != null) {
      info.append(", ");
    }
    if (game.blackUsername() != null) {
      info.append("Black: ").append(game.blackUsername());
    }
    info.append(")");
    return info.toString();
  }

  private void joinGame() throws Exception {
    if (gamesList.isEmpty()) {
      System.out.println("Please list games first using 'list' command");
      return;
    }

    System.out.print("Enter game number (1-" + gamesList.size() + "): ");
    int gameNumber=Integer.parseInt(scanner.nextLine());

    if (gameNumber < 1 || gameNumber > gamesList.size()) {
      throw new IllegalArgumentException("Invalid game number");
    }

    System.out.print("Enter color to play (WHITE/BLACK): ");
    String color=scanner.nextLine().toUpperCase();
    if (!color.equals("WHITE") && !color.equals("BLACK")) {
      throw new IllegalArgumentException("Invalid color. Must be WHITE or BLACK");
    }

    GameData selectedGame=gamesList.get(gameNumber - 1);
    server.joinGame(color, selectedGame.gameID(), authToken);

    // Display the chess board
    displayGame(selectedGame);
  }

  private void observeGame() throws Exception {
    if (gamesList.isEmpty()) {
      System.out.println("Please list games first using 'list' command");
      return;
    }

    System.out.print("Enter game number to observe (1-" + gamesList.size() + "): ");
    int gameNumber=Integer.parseInt(scanner.nextLine());

    if (gameNumber < 1 || gameNumber > gamesList.size()) {
      throw new IllegalArgumentException("Invalid game number");
    }

    GameData selectedGame=gamesList.get(gameNumber);
    // Join as observer (null color)
    server.observeGame(selectedGame.gameID(), authToken);

    // Display the chess board
    displayGame(selectedGame);
  }

  private void displayGame(GameData game) {
    System.out.println("\nGame: " + game.gameName());
    if (game.whiteUsername() != null) {
      System.out.println("White: " + game.whiteUsername());
    }
    if (game.blackUsername() != null) {
      System.out.println("Black: " + game.blackUsername());
    }
    System.out.println();

    // Display board in both orientations
    if (game.game() == null) {
      ChessGame newGame=new ChessGame();
      ChessBoard board=new ChessBoard();
      board.resetBoard();
      newGame.setBoard(board);
      ChessBoardUI.displayGame(board);
    } else {
      ChessBoardUI.displayGame(game.game().getBoard());
    }
  }

}
package ui;

import chess.ChessBoard;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import client.ServerFacade;
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
  private GameData currentGame = null;
  private ChessGame.TeamColor playerColor = null;
  private boolean inGame = false;

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

  private void observeGame() {
    if (gamesList.isEmpty()) {
      System.out.println("Please list games first using 'list' command");
      return;
    }

    System.out.print("Enter game number (1-" + gamesList.size() + "): ");
    int gameNumber = Integer.parseInt(scanner.nextLine());
    if (gameNumber < 1 || gameNumber > gamesList.size()) {
      throw new IllegalArgumentException("Invalid game number");
    }

    GameData selectedGame = gamesList.get(gameNumber - 1);
    displayGame(selectedGame);  // Display the game's current state
  }

  private void displayGame(GameData game) {
    currentGame = game;
    System.out.println("\nGame: " + game.gameName());
    if (game.whiteUsername() != null) {
      System.out.println("White: " + game.whiteUsername());
    }
    if (game.blackUsername() != null) {
      System.out.println("Black: " + game.blackUsername());
    }
    System.out.println();

    // Display board in both orientations
    ChessGame chessGame;
    if (game.game() == null) {
      ChessGame newGame=new ChessGame();
      ChessBoard board=new ChessBoard();
      board.resetBoard();
      newGame.setBoard(board);
      ChessBoardUI.displayGame(board);
    } else {
      ChessBoardUI.displayGame(game.game().getBoard());
    }
    startGameplay(game);
  }


  private void startGameplay(GameData game) {
    try {
      // Connect to WebSocket
      server.connectToGame(game.gameID(), authToken);
      inGame = true;

      // Start gameplay loop
      gameplayUI();
    } catch (Exception e) {
      System.out.println("Error connecting to game: " + e.getMessage());
    }
  }

  private void gameplayUI() {
    while (inGame) {
      displayGameplayMenu();
      String command = scanner.nextLine().toLowerCase();

      try {
        switch (command) {
          case "help" -> displayGameplayHelp();
          case "redraw" -> redrawBoard();
          case "leave" -> leaveGame();
          case "move" -> makeMove();
          case "resign" -> resignGame();
          case "highlight" -> highlightMoves();
          default -> System.out.println("Unknown command. Type 'help' for list of commands.");
        }
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }
  }

  private void displayGameplayMenu() {
    System.out.println("""
                
                Game Commands:
                  help - Display available commands
                  redraw - Redraw the chess board
                  leave - Leave the game
                  move - Make a move
                  resign - Resign the game
                  highlight - Show legal moves for a piece
                """);
  }

  private void displayGameplayHelp() {
    System.out.println("""
                Available game commands:
                  help - Display this help message
                  redraw - Redraw the chess board
                  leave - Leave the game and return to main menu
                  move - Make a move on the board
                  resign - Resign from the game
                  highlight - Show legal moves for a selected piece
                """);
  }

  private void redrawBoard() {
    if (currentGame != null && currentGame.game() != null) {
      ChessBoardUI.displayGame(currentGame.game().getBoard());
    }
  }

  private void leaveGame() throws Exception {
    if (currentGame != null) {
      server.leaveGame(currentGame.gameID(), authToken);
      inGame = false;
      currentGame = null;
      playerColor = null;
    }
  }

  private void makeMove() throws Exception {
    if (currentGame == null) return;

    System.out.print("Enter starting position (e.g., e2): ");
    String start = scanner.nextLine().toLowerCase();
    System.out.print("Enter ending position (e.g., e4): ");
    String end = scanner.nextLine().toLowerCase();

    try {
      ChessMove move = parseMove(start, end);
      server.makeMove(currentGame.gameID(), authToken, move);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid move format. Use format 'e2 e4'");
    }
  }

  private void resignGame() throws Exception {
    if (currentGame == null) return;

    System.out.print("Are you sure you want to resign? (yes/no): ");
    String confirm = scanner.nextLine().toLowerCase();
    if (confirm.equals("yes")) {
      server.resignGame(currentGame.gameID(), authToken);
      System.out.println("You have resigned from the game.");
    }
  }

  private void highlightMoves() {
    if (currentGame == null || currentGame.game() == null) return;

    System.out.print("Enter piece position to highlight (e.g., e2): ");
    String position = scanner.nextLine().toLowerCase();

    try {
      ChessPosition pos = parsePosition(position);
      ChessGame game = currentGame.game();
      Collection<ChessMove> moves = game.validMoves(pos);

      // Display board with highlights
      ChessBoardUI.displayGameWithHighlights(game.getBoard(), moves);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid position format. Use format 'e2'");
    }
  }

  // Helper methods for UI updates from ServerFacade
  public void updateGameDisplay(ChessGame game) {
    if (currentGame != null) {
      currentGame.setGame(game);
      redrawBoard();
    }
  }

  public void displayError(String message) {
    System.out.println("Error: " + message);
  }

  public void displayNotification(String message) {
    System.out.println("Notification: " + message);
  }

  // Helper methods for parsing chess positions and moves
  private ChessPosition parsePosition(String pos) {
    if (pos.length() != 2) throw new IllegalArgumentException("Invalid position format");

    int col = pos.charAt(0) - 'a';
    int row = Character.getNumericValue(pos.charAt(1)) - 1;

    if (col < 0 || col > 7 || row < 0 || row > 7) {
      throw new IllegalArgumentException("Position out of bounds");
    }

    return new ChessPosition(row, col);
  }

  private ChessMove parseMove(String start, String end) {
    ChessPosition startPos = parsePosition(start);
    ChessPosition endPos = parsePosition(end);
    return new ChessMove(startPos, endPos);
  }
}

}
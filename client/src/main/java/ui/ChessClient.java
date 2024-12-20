package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.WebSocketCommunicator;
import model.AuthData;
import model.GameData;
import client.ServerFacade;
import client.websocket.ServerMessageObserver;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Collection;
import websocket.messages.*;

public class ChessClient {
  private final ServerFacade server;
  private final Scanner scanner;
  private String authToken=null;
  private List<GameData> gamesList=new ArrayList<>();
  private GameData currentGame = null;
  private ChessGame.TeamColor playerColor = null;
  private boolean inGame = false;
  private WebSocketCommunicator webSocket;
  private final String serverUrl;

  public ChessClient(String serverUrl) {
    this.server=new ServerFacade(serverUrl, this);
    this.scanner=new Scanner(System.in);
    this.serverUrl= serverUrl;
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
    if (webSocket != null) {
      webSocket.close();
      webSocket = null;
    }
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
    ChessGame chessGame;
    if (game.game() == null) {
      chessGame = new ChessGame();
      ChessBoard board=new ChessBoard();
      board.resetBoard();
      chessGame.setBoard(board);
    } else {
      chessGame = game.game();
    }
    determinePerspective(game);
    ChessBoardUI.displayGame(chessGame.getBoard(), getPerspective());
    startGameplay(game);
  }
  private void determinePerspective(GameData game) {
    String username = server.getUsername(); // You'll need to add this method to ServerFacade
    if (username.equals(game.whiteUsername())) {
      playerColor = ChessGame.TeamColor.WHITE;
    } else if (username.equals(game.blackUsername())) {
      playerColor = ChessGame.TeamColor.BLACK;
    } else {
      playerColor = null; // Observer
    }
  }
  private ChessGame.TeamColor getPerspective() {
    // Observers see from white's perspective, players see from their color's perspective
    return playerColor != null ? playerColor : ChessGame.TeamColor.WHITE;
  }
  private void startGameplay(GameData game) {
    try {
      URI uri = new URI("ws://" + serverUrl + "/connect");
      webSocket = new WebSocketCommunicator(uri, new ServerMessageObserver() {
        @Override
        public void notify(ServerMessage message) {
          handleMessage(message);
        }
      });
      if (webSocket.connectBlocking()) {
        webSocket.connectToGame(game.gameID(), authToken);
        inGame = true;
        gameplayUI();
      } else {
        throw new Exception("Failed to connect to game server");
      }
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
      ChessBoardUI.displayGame(currentGame.game().getBoard(), getPerspective());
    }
  }

  private void makeMove() throws Exception {
    if (currentGame == null) {return;}
    if (playerColor == null) {
      System.out.println("Observers cannot make moves");
      return;
    }
    if (currentGame.game().getTeamTurn() != playerColor) {
      System.out.println("It's not your turn");
      return;
    }
    System.out.print("Enter starting position (e.g., e2): ");
    String start = scanner.nextLine().toLowerCase();
    System.out.print("Enter ending position (e.g., e4): ");
    String end = scanner.nextLine().toLowerCase();
    try {
      ChessMove move = parseMove(start, end);
      ChessPiece piece = currentGame.game().getBoard().getPiece(move.getStartPosition());
      if (piece == null || piece.getTeamColor() != playerColor) {
        System.out.println("You can only move your own pieces");
        return;
      }
      webSocket.sendMove(move);
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid move format. Use format 'e2 e4'");
    }
  }

  private void highlightMoves() {
    if (currentGame == null || currentGame.game() == null) {return;}
    System.out.print("Enter piece position to highlight (e.g., e2): ");
    String position = scanner.nextLine().toLowerCase();
    try {
      ChessPosition pos = parsePosition(position);
      ChessGame game = currentGame.game();
      Collection<ChessMove> moves = game.validMoves(pos);
      if (moves.isEmpty()) {
        System.out.println("No legal moves for this piece");
        return;
      }
      ChessBoardUI.displayGameWithHighlights(game.getBoard(), moves, getPerspective());
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid position format. Use format 'e2'");
    }
  }

  public void updateGameDisplay(GameData game) {
    if (currentGame != null) {
      currentGame = game;
      System.out.println("\nBoard updated:"); // Add notification of update
      redrawBoard();
    }
  }

  private void resignGame() throws Exception {
    if (currentGame == null) {return;}
    if (playerColor == null) {
      System.out.println("Observers cannot resign");
      return;
    }
    System.out.print("Are you sure you want to resign? (yes/no): ");
    String confirm = scanner.nextLine().toLowerCase();
    if (confirm.equals("yes")) {
      webSocket.resignGame();
      System.out.println("You have resigned from the game.");
    }
  }

  private void leaveGame() throws Exception {
    if (currentGame != null) {
      if (webSocket != null) {
          webSocket.leaveGame();
          webSocket.close();
          webSocket = null;
      }
      inGame = false;
      currentGame = null;
      playerColor = null;
      postloginUI();
    }
  }

  public void handleMessage(ServerMessage message) {
    switch (message.getServerMessageType()) {
      case NOTIFICATION -> {
        NotificationMessage notification = (NotificationMessage) message;
        displayNotification(notification.getMessage());
      }
      case ERROR -> {
        ErrorMessage error = (ErrorMessage) message;
        displayError(error.getErrorMessage());
      }
      case LOAD_GAME -> {
        LoadGameMessage loadGame = (LoadGameMessage) message;
        updateGameDisplay(loadGame.getGame());
      }
    }
  }
  public void displayError(String message) {
    System.out.println("Error: " + message);
  }
  public void displayNotification(String message) {
    System.out.println("Notification: " + message);
  }
  private ChessPosition parsePosition(String pos) {
    if (pos.length() != 2) {throw new IllegalArgumentException("Invalid position format");}
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
    ChessPiece piece = currentGame.game().getBoard().getPiece(startPos);
    if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
      int lastRank = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 7 : 0;
      if (endPos.getRow() == lastRank) {
        ChessPiece.PieceType promotionPiece = getPromotionPieceFromUser();
        return new ChessMove(startPos, endPos, promotionPiece);
      }
    }
    return new ChessMove(startPos, endPos, null);
  }
  private ChessPiece.PieceType getPromotionPieceFromUser() {
    while (true) {
      System.out.println("Promote pawn to (Q)ueen, (R)ook, (B)ishop, or k(N)ight? ");
      String input = scanner.nextLine().toUpperCase();
      return switch (input) {
        case "Q" -> ChessPiece.PieceType.QUEEN;
        case "R" -> ChessPiece.PieceType.ROOK;
        case "B" -> ChessPiece.PieceType.BISHOP;
        case "N" -> ChessPiece.PieceType.KNIGHT;
        default -> throw new IllegalStateException("Unexpected value: " + input);
      };
    }
}
}

package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import static ui.EscapeSequences.*;

public class ChessBoardUI {
  private static final int BOARD_SIZE = 8;

  public static void displayGame(ChessBoard board) {
    // Display both board orientations
    System.out.println("\nBLACK PERSPECTIVE");
    displayBoardBlack(board);
    System.out.println("\nWHITE PERSPECTIVE");
    displayBoardWhite(board);
  }

  private static void displayBoardWhite(ChessBoard board) {
    printHeaders(false);
    for (int row = BOARD_SIZE; row >= 1; row--) {
      printRow(row, board, false);
    }
    printHeaders(false);
  }

  private static void displayBoardBlack(ChessBoard board) {
    printHeaders(true);
    for (int row = 1; row <= BOARD_SIZE; row++) {
      printRow(row, board, true);
    }
    printHeaders(true);
  }

  private static void printHeaders(boolean reversed) {
    System.out.print("     ");
    for (char col = reversed ? 'h' : 'a';
         reversed ? col >= 'a' : col <= 'h';
         col = (char) (reversed ? col - 1 : col + 1)) {
       System.out.print(col + "  ");
    }
    System.out.println();
  }


  private static void printRow(int row, ChessBoard board, boolean reversed) {
    // Print row number
    System.out.print(" " + row + " ");

    // Print squares
    for (int col = (reversed ? BOARD_SIZE : 1);
         reversed ? col >= 1 : col <= BOARD_SIZE;
         col = (reversed ? col - 1 : col + 1)) {

      boolean isLightSquare = ((row + col) % 2 != 0);
      ChessPosition position = new ChessPosition(row, col);
      ChessPiece piece = board.getPiece(position);


      // Set square background color
      System.out.print(isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY);

      // Print piece or empty square
      if (piece == null) {
        System.out.print("   ");
      } else {
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
        System.out.print(color + " " + getPieceString(piece) + " ");
      }
      System.out.print(RESET_TEXT_COLOR);
    }

    // Reset colors and print row number again
    System.out.println(RESET_BG_COLOR + " " + row + " ");
  }

  private static String getPieceString(ChessPiece piece) {
    return switch (piece.getPieceType()) {
      case KING -> "K";
      case QUEEN -> "Q";
      case BISHOP -> "B";
      case KNIGHT -> "N";
      case ROOK -> "R";
      case PAWN -> "P";
    };
  }
}
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
    System.out.println(SET_TEXT_BOLD + "BLACK PERSPECTIVE" + RESET_TEXT_BOLD_FAINT);
    displayBoardBlack(board);
    System.out.println("\n" + SET_TEXT_BOLD + "WHITE PERSPECTIVE" + RESET_TEXT_BOLD_FAINT);
    displayBoardWhite(board);
  }

  private static void displayBoardWhite(ChessBoard board) {
    printHeaders();
    for (int row = BOARD_SIZE; row >= 1; row--) {
      printRow(row, board, false);
    }
    printHeaders();
  }

  private static void displayBoardBlack(ChessBoard board) {
    printHeadersReversed();
    for (int row = 1; row <= BOARD_SIZE; row++) {
      printRow(row, board, true);
    }
    printHeadersReversed();
  }

  private static void printHeaders() {
    System.out.print("    ");
    for (char col = 'a'; col <= 'h'; col++) {
      System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + col + " ");
    }
    System.out.println(RESET_BG_COLOR);
  }

  private static void printHeadersReversed() {
    System.out.print("    ");
    for (char col = 'h'; col >= 'a'; col--) {
      System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + col + " ");
    }
    System.out.println(RESET_BG_COLOR);
  }

  private static void printRow(int row, ChessBoard board, boolean reversed) {
    // Print row number
    System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR);

    // Print squares
    for (int col = (reversed ? BOARD_SIZE : 1);
         reversed ? col >= 1 : col <= BOARD_SIZE;
         col = (reversed ? col - 1 : col + 1)) {

      boolean isLightSquare = (row + col) % 2 == 0;
      ChessPosition position = new ChessPosition(row, col);
      ChessPiece piece = board.getPiece(position);

      // Set square background color
      System.out.print(isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY);

      // Print piece or empty square
      if (piece == null) {
        System.out.print(EMPTY);
      } else {
        System.out.print(getPieceString(piece));
      }
    }

    // Reset colors and print row number again
    System.out.println(RESET_BG_COLOR + SET_BG_COLOR_LIGHT_GREY +
            SET_TEXT_COLOR_BLACK + " " + row + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
  }

  private static String getPieceString(ChessPiece piece) {
    boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
    return switch (piece.getPieceType()) {
      case KING -> isWhite ? WHITE_KING : BLACK_KING;
      case QUEEN -> isWhite ? WHITE_QUEEN : BLACK_QUEEN;
      case BISHOP -> isWhite ? WHITE_BISHOP : BLACK_BISHOP;
      case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
      case ROOK -> isWhite ? WHITE_ROOK : BLACK_ROOK;
      case PAWN -> isWhite ? WHITE_PAWN : BLACK_PAWN;
    };
  }
}
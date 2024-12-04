package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessMove;
import chess.ChessPosition;
import static ui.EscapeSequences.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;


public class ChessBoardUI {
  private static final int BOARD_SIZE = 8;
  private static final String HIGHLIGHT_BG_COLOR = SET_BG_COLOR_GREEN;


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
    System.out.print("    ");
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

  public static void displayGameWithHighlights(ChessBoard board, Collection<ChessMove> highlights) {
    // Create sets of highlighted positions for easier lookup
    Set<ChessPosition> highlightedPositions = new HashSet<>();
    ChessPosition selectedPosition = null;

    for (ChessMove move : highlights) {
      highlightedPositions.add(move.getEndPosition());
      selectedPosition = move.getStartPosition(); // All moves share the same start position
    }

    // Display both perspectives with highlights
    System.out.println("\nBLACK PERSPECTIVE");
    displayBoardBlackWithHighlights(board, highlightedPositions, selectedPosition);
    System.out.println("\nWHITE PERSPECTIVE");
    displayBoardWhiteWithHighlights(board, highlightedPositions, selectedPosition);
  }

  private static void displayBoardWhiteWithHighlights(ChessBoard board, Set<ChessPosition> highlights, ChessPosition selectedPos) {
    printHeaders(false);
    for (int row = BOARD_SIZE; row >= 1; row--) {
      printRowWithHighlights(row, board, false, highlights, selectedPos);
    }
    printHeaders(false);
  }

  private static void displayBoardBlackWithHighlights(ChessBoard board, Set<ChessPosition> highlights, ChessPosition selectedPos) {
    printHeaders(true);
    for (int row = 1; row <= BOARD_SIZE; row++) {
      printRowWithHighlights(row, board, true, highlights, selectedPos);
    }
    printHeaders(true);
  }

  private static void printRowWithHighlights(int row, ChessBoard board, boolean reversed,
                                             Set<ChessPosition> highlights, ChessPosition selectedPos) {
    // Print row number
    System.out.print(" " + row + " ");

    // Print squares
    for (int col = (reversed ? BOARD_SIZE : 1);
         reversed ? col >= 1 : col <= BOARD_SIZE;
         col = (reversed ? col - 1 : col + 1)) {

      ChessPosition position = new ChessPosition(row, col);
      ChessPiece piece = board.getPiece(position);
      boolean isLightSquare = ((row + col) % 2 != 0);

      // Determine square background color
      String bgColor;
      if (position.equals(selectedPos)) {
        bgColor = SET_BG_COLOR_YELLOW; // Highlight selected piece
      } else if (highlights.contains(position)) {
        bgColor = HIGHLIGHT_BG_COLOR; // Highlight possible moves
      } else {
        bgColor = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY;
      }
      System.out.print(bgColor);

      // Print piece or empty square
      if (piece == null) {
        System.out.print("   ");
      } else {
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
        System.out.print(color + " " + getPieceString(piece) + " ");
      }
      System.out.print(RESET_TEXT_COLOR);
    }

    // Reset colors and print row number again
    System.out.println(RESET_BG_COLOR + " " + row + " ");
  }
}
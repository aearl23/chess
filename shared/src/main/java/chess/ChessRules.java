package chess;

import java.util.*;

public class ChessRules {

  @FunctionalInterface
  public interface MoveRules {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
  }

  //moves for all pieces
  private final Map<ChessPiece.PieceType, MoveRules> rulesMap = new HashMap<>();

  public ChessRules() {
      // Define the movement rules for each piece type
      rulesMap.put(ChessPiece.PieceType.KING, this::kingMoves);
      rulesMap.put(ChessPiece.PieceType.QUEEN, this::queenMoves);
      rulesMap.put(ChessPiece.PieceType.BISHOP, this::bishopMoves);
      rulesMap.put(ChessPiece.PieceType.KNIGHT, this::knightMoves);
      rulesMap.put(ChessPiece.PieceType.ROOK, this::rookMoves);
      rulesMap.put(ChessPiece.PieceType.PAWN, this::pawnMoves);
  }

  public Collection<ChessMove> pieceRule(ChessPiece.PieceType pieceType, ChessBoard board, ChessPosition position) {
    MoveRules rules = rulesMap.get(pieceType);
    if (rules != null) {
      return rules.pieceMoves(board, position);
    }
    return new ArrayList<>();
  }

  // Example rules
  // Implementations for various piece moves
  private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition position) {
    return generateMoves(board, position, new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    }, 1);
  }

  private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition position) {
    return generateMoves(board, position, new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    }, 8);
  }

  private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition position) {
    return generateMoves(board, position, new int[][]{
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    }, 8);
  }

  private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition position) {
    Collection<ChessMove> moves = new ArrayList<>();
    int[][] knightDirections = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };
    addValidMoves(board, position,  knightDirections, moves, 1);
    return moves;
  }

  private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition position) {
    return generateMoves(board, position, new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    }, 8);
  }

  private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition position) {
    Collection<ChessMove> moves = new ArrayList<>();

    // Get the piece at the current position
    ChessPiece piece = board.getPiece(position);
    if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) {
      return moves;  // No piece or not a pawn at the given position
    }

    // Determine direction based on color
    int direction = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

    // Promotion row (row 8 for white, row 1 for black)
    int promotionRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

    // Forward move (one step)
    ChessPosition forwardPosition = new ChessPosition(position.getRow() + direction, position.getColumn());
    if (board.getPiece(forwardPosition) == null) {
      if(forwardPosition.getRow() == promotionRow) {
          moves.add(new ChessMove(position, forwardPosition, ChessPiece.PieceType.QUEEN));
          moves.add(new ChessMove(position, forwardPosition, ChessPiece.PieceType.ROOK));
          moves.add(new ChessMove(position, forwardPosition, ChessPiece.PieceType.KNIGHT));
          moves.add(new ChessMove(position, forwardPosition, ChessPiece.PieceType.BISHOP));
      } else {
          moves.add(new ChessMove(position, forwardPosition, null));
      }
    }

    // Forward move (two steps from starting row)
    int startingRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
    if (position.getRow() == startingRow) {
      ChessPosition twoStepForward = new ChessPosition(position.getRow() + 2 * direction, position.getColumn());
      if (board.getPiece(forwardPosition) == null && board.getPiece(twoStepForward) == null) {
        moves.add(new ChessMove(position, twoStepForward, null));
      }
    }

    // Capture moves (diagonal)
    int[][] captureDirections = {{direction, 1}, {direction, -1}};
    for (int[] captureDirection : captureDirections) {
      ChessPosition capturePosition = new ChessPosition(position.getRow() + captureDirection[0], position.getColumn() + captureDirection[1]);
      if(capturePosition.getRow() <= 0 || capturePosition. getRow() > 8 || capturePosition.getColumn() <= 0 || capturePosition.getColumn() > 8){
        continue;
      }
      ChessPiece pieceAtCapturePosition = board.getPiece(capturePosition);
      if (pieceAtCapturePosition != null && pieceAtCapturePosition.getTeamColor() != piece.getTeamColor()) {
        if(capturePosition.getRow() == promotionRow) {
          moves.add(new ChessMove(position, capturePosition, ChessPiece.PieceType.QUEEN));  // Capture opponent's piece
          moves.add(new ChessMove(position, capturePosition, ChessPiece.PieceType.KNIGHT));// Capture opponent's piece
          moves.add(new ChessMove(position, capturePosition, ChessPiece.PieceType.ROOK));  // Capture opponent's piece
          moves.add(new ChessMove(position, capturePosition, ChessPiece.PieceType.BISHOP));  // Capture opponent's piece
        } else {
          moves.add(new ChessMove(position, capturePosition,null));  // Capture opponent's piece
        }
      }
    }

    return moves;
  }

  // Helper functions to generate and validate moves
  private Collection<ChessMove> generateMoves(ChessBoard board, ChessPosition position, int[][] directions, int maxSteps) {
    Collection<ChessMove> moves = new ArrayList<>();
    addValidMoves(board, position, directions, moves, maxSteps);
    return moves;
  }

  private void addValidMoves(ChessBoard board, ChessPosition position, int[][] directions, Collection<ChessMove> moves, int maxSteps) {
    for (int[] direction : directions) {
      int row = position.getRow();
      int col = position.getColumn();

      for (int step = 0; step < maxSteps; step++) {
        row += direction[0];
        col += direction[1];

        if (row < 1 || row > 8 || col < 1 || col > 8) {
          break;  // Out of bounds
        }

        ChessPosition newPosition = new ChessPosition(row, col);
        ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
        ChessPiece piece = board.getPiece(position);

        if (pieceAtNewPosition == null) {
          moves.add(new ChessMove(position, newPosition,null));  // Empty square
        } else if (pieceAtNewPosition.getTeamColor() != piece.getTeamColor()) {
          moves.add(new ChessMove(position, newPosition, null));  // Opponent's piece
          break;  // Stop further movement in this direction
        } else {
          break;  // Our own piece, stop
        }
      }
    }
  }
}


//look into data table abstraction, single line rules for all move rules
//use hashmap to map rules to pieces
//rules.pieceRule(King).moves(board, position);

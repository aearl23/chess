package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ChessGame {
  private ChessBoard board;
  private TeamColor currentTurn;
  private ChessRules rules;
  private boolean isGameOver;
  private TeamColor winner;

  public ChessGame() {
    this.board = new ChessBoard();
    this.board.resetBoard();
    this.currentTurn = TeamColor.WHITE;
    this.rules = new ChessRules();
    this.isGameOver = false;
    this.winner = null;
  }


  // Add getter method for game over state
  public boolean isGameOver() {
    // Game is over if explicitly set (resignation) or if there's checkmate/stalemate
    return isGameOver || isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK)
            || isInStalemate(TeamColor.WHITE) || isInStalemate(TeamColor.BLACK);
  }

  // Add getter for winner
  public TeamColor getWinner() {
    return winner;
  }

  public TeamColor getTeamTurn() {
    return currentTurn;
  }

  public void setTeamTurn(TeamColor team) {
    this.currentTurn = team;
  }

  public enum TeamColor {
    WHITE,
    BLACK
  }

  public Collection<ChessMove> validMoves(ChessPosition startPosition) {
    ChessPiece piece = board.getPiece(startPosition);
    if (piece == null) {
      return Collections.emptyList();
    }
    return filterValidMoves(piece, startPosition);
  }

  private Collection<ChessMove> filterValidMoves(ChessPiece piece, ChessPosition startPosition) {
    Collection<ChessMove> potentialMoves = rules.getPieceMoves(piece.getPieceType(), board, startPosition);
    Collection<ChessMove> validMoves = new ArrayList<>();

    for (ChessMove move : potentialMoves) {
      if (isMoveValid(move, piece)) {
        validMoves.add(move);
      }
    }
    return validMoves;
  }

  private boolean isMoveValid(ChessMove move, ChessPiece piece) {
    ChessBoard tempBoard = board.copy();
    makeTemporaryMove(tempBoard, move, piece);
    return !isInCheckAfterMove(piece.getTeamColor(), tempBoard);
  }

  private void makeTemporaryMove(ChessBoard tempBoard, ChessMove move, ChessPiece piece) {
    tempBoard.removePiece(move.getStartPosition());
    tempBoard.removePiece(move.getEndPosition());
    tempBoard.addPiece(move.getEndPosition(), piece);
  }

  public void makeMove(ChessMove move) throws InvalidMoveException {
    if (isGameOver()) {
      throw new InvalidMoveException("Game is already over");
    }

    validateMove(move);
    executeMove(move);
    switchTurns();

    // Check for checkmate or stalemate after move
    TeamColor opponent = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    if (isInCheckmate(opponent)) {
      isGameOver = true;
      winner = (opponent == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    } else if (isInStalemate(opponent)) {
      isGameOver = true;
      winner = null; // Stalemate means no winner
    }
  }

  private void validateMove(ChessMove move) throws InvalidMoveException {
    ChessPiece piece = board.getPiece(move.getStartPosition());
    if (piece == null || piece.getTeamColor() != currentTurn) {
      throw new InvalidMoveException("Invalid move");
    }
    if (!validMoves(move.getStartPosition()).contains(move)) {
      throw new InvalidMoveException("Invalid move");
    }
  }

  private void executeMove(ChessMove move) {
    ChessPiece piece = board.getPiece(move.getStartPosition());
    board.removePiece(move.getStartPosition());
    board.removePiece(move.getEndPosition());

    if (move.getPromotionPiece() != null) {
      board.addPiece(move.getEndPosition(), new ChessPiece(currentTurn, move.getPromotionPiece()));
    } else {
      board.addPiece(move.getEndPosition(), piece);
    }
  }

  private void switchTurns() {
    currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
  }

  public boolean isInCheck(TeamColor teamColor) {
    ChessPosition kingPosition = findKing(teamColor, board);
    if (kingPosition == null) {
      return false;
    }
    return isKingThreatened(kingPosition, teamColor, board);
  }

  private boolean isKingThreatened(ChessPosition kingPosition, TeamColor teamColor, ChessBoard targetBoard) {
    TeamColor opponent = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        if (canPositionThreatenKing(new ChessPosition(row, col), kingPosition, opponent, targetBoard)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean canPositionThreatenKing(ChessPosition position, ChessPosition kingPosition,
                                          TeamColor opponent, ChessBoard targetBoard) {
    ChessPiece piece = targetBoard.getPiece(position);
    if (piece == null || piece.getTeamColor() != opponent) {
      return false;
    }
    Collection<ChessMove> moves = rules.getPieceMoves(piece.getPieceType(), targetBoard, position);
    return moves.stream().anyMatch(move -> move.getEndPosition().equals(kingPosition));
  }


  public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
    if (!isInCheck(teamColor)) {
      return false;
    }
    return hasNoValidMoves(teamColor);
  }

  public boolean isInStalemate(TeamColor teamColor) {
    if (isInCheck(teamColor)) {
      return false;
    }
    return hasNoValidMoves(teamColor);
  }

  private boolean hasNoValidMoves(TeamColor teamColor) {
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        if (pieceHasValidMoves(new ChessPosition(row, col), teamColor)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean pieceHasValidMoves(ChessPosition position, TeamColor teamColor) {
    ChessPiece piece = board.getPiece(position);
    return piece != null &&
            piece.getTeamColor() == teamColor &&
            !validMoves(position).isEmpty();
  }

  private boolean isInCheckAfterMove(TeamColor teamColor, ChessBoard tempBoard) {
    ChessPosition kingPosition = findKing(teamColor, tempBoard);
    if (kingPosition == null) {
      return false;
    }
    return isKingThreatened(kingPosition, teamColor, tempBoard);
  }

  private ChessPosition findKing(TeamColor teamColor, ChessBoard targetBoard) {
    for (int row = 1; row <= 8; row++) {
      for (int col = 1; col <= 8; col++) {
        ChessPosition position = new ChessPosition(row, col);
        if (isKingAtPosition(position, teamColor, targetBoard)) {
          return position;
        }
      }
    }
    return null;
  }

  private boolean isKingAtPosition(ChessPosition position, TeamColor teamColor, ChessBoard targetBoard) {
    ChessPiece piece = targetBoard.getPiece(position);
    return piece != null &&
            piece.getPieceType() == ChessPiece.PieceType.KING &&
            piece.getTeamColor() == teamColor;
  }

  public void setBoard(ChessBoard board) {
    this.board = board;
  }

  public ChessBoard getBoard() {
    return board;
  }

  public void setGameOver(boolean resigned) {
    if (resigned) {
      isGameOver = true;
      // The winner is the opposite of current turn when someone resigns
      winner = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }
  }

}


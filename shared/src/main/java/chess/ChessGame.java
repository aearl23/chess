package chess;

import java.util.Collection;
import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;
    private ChessRules rules;
    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = TeamColor.WHITE; //White goes first
        this.rules = new ChessRules();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null || piece.getTeamColor() != currentTurn){
            return new ArrayList<>(); //empty list of moves
        }
        return rules.pieceRule(piece.getPieceType(), board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)){
            throw new InvalidMoveException("Invalid Move:" + move);
        }
        //make the move on the board
        ChessPiece movingpiece = board.getPiece(move.getStartPosition());
        board.removePiece(move.getStartPosition());

        board.removePiece(move.getEndPosition()); //Remove captured piece too

        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(movingpiece.getTeamColor(), move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), movingpiece);
        }
        // Switch turns
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }
        // get captured piece at target location
        //ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
        //ChessMove updatedMove = new ChessMove(move.getStartPosition(), move.getEndPosition(), capturedPiece.getPieceType(), capturedPiece);
        //board.makeMove(move);
        //currentTurn = (currentTurn ==TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

    public boolean isInCheck(TeamColor teamColor) {
        //find position of king for given team
        //Look for opposing pieces
        //Check to see if any moves by an opposing piece can capture the king
        //Return true if a valid move exist

        ChessPosition kingPosition = findKing(teamColor);
        if (kingPosition == null) {
            return false;
        }
        TeamColor opponent = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        //grab all pieces on the board
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                //Check if any present piece has a potential move that threatens the King
                if (piece != null && piece.getTeamColor() == opponent) {
                    Collection<ChessMove> moves = rules.pieceRule(piece.getPieceType(), board, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)){
                            return true; //King is in check
                        }
                    }
                }
            }
        }
        return false; //No moves threatening the King
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false; //cant be in checkmate if not in check
        }//check if any move can get the king out of check

        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    for (ChessMove move : moves) {
                        ChessBoard tempboard = board.copy();
                        tempboard.removePiece(move.getStartPosition());
                        tempboard.removePiece(move.getEndPosition());

                        if (move.getPromotionPiece() != null) {
                            tempboard.addPiece(move.getEndPosition(), new ChessPiece(teamColor, move.getPromotionPiece()));
                            // Found a move that gets out of check
                        } else {
                            tempboard.addPiece(move.getEndPosition(), piece);
                        }
                        if (!isInCheckAfterMove(teamColor, tempboard)){
                            return false;
                        }
                    }
                }
            }
        }
        return true; // No move gets out of check, it's checkmate
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;  //cant be in stalemate if not in check
        }
        //check if team has valid moves to get out of check
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> moves = validMoves(position);
                    if (!moves.isEmpty()){
                        return false; //not in stalemate
                    }
                }
            }
        }
        return true; //no moves, piece is in stalemate
    }


    private ChessPosition findKing(TeamColor teamColor){
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8 ; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                    return position;
                }
            }
        }
        return null;
    }

    private boolean isInCheckAfterMove(TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPosition = findKingOnBoard(teamColor, tempBoard);
        if (kingPosition == null) {
            return false; // No king found (shouldn't happen in a valid game)
        }

        TeamColor oppositeTeam = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = tempBoard.getPiece(position);

                if (piece != null && piece.getTeamColor() == oppositeTeam) {
                    Collection<ChessMove> moves = rules.pieceRule(piece.getPieceType(), tempBoard, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // King is still under attack
                        }
                    }
                }
            }
        }

        return false; // King is not under attack after the move
    }

    private ChessPosition findKingOnBoard(TeamColor teamColor, ChessBoard board) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null; // King not found (shouldn't happen in a valid game)
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }
}

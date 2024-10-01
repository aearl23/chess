package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1]=piece;
    }
    /**throw new RuntimeException("Not implemented");
     *
     */
    public ChessPiece removePiece(ChessPosition position){
        int row = position.getRow() -1;
        int col = position.getColumn() -1;

        if (row < 0 || row >= 8 || col < 0 || col >= 8){
            return null; //out of bounds
        }
        ChessPiece removedpiece = board[row][col];
        board[row][col] = null;
        return removedpiece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.board = new ChessPiece[8][8];

        //layout pieces for back Row
        ChessPiece.PieceType[] backRow = {ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK};

        //add pieces to the board
        //row of pawn and back row pieces for white and black
        //white first, then black
        for (int i = 0; i < 8; i++){
            //add white pawns first
            addPiece(new ChessPosition(2,i+1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            //add white back line
            addPiece(new ChessPosition(1, i + 1), new ChessPiece(ChessGame.TeamColor.WHITE, backRow[i]));
        }

        for(int i = 0; i < 8; i++){
            addPiece(new ChessPosition(7, i+1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, i+1), new ChessPiece(ChessGame.TeamColor.BLACK, backRow[i]));
        }
    }

    public ChessBoard copy() {
        ChessBoard newBoard = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = this.getPiece(position);
                if (piece != null) {
                    newBoard.addPiece(position, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return newBoard;
    }

    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof ChessBoard that)) return false;
//      return Arrays.deepEquals(board, that.board);
//    }
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard other = (ChessBoard) o;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!Objects.deepEquals(this.board[i][j], other.board[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "board=" + Arrays.deepToString(board) +
                '}';
    }
    //loop through and define pieces by char to see the board and debug from there

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}


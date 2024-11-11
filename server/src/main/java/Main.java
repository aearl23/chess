import chess.*;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import javax.xml.crypto.Data;

public class Main {
    public static void main(String[] args) throws DataAccessException {
            var piece=new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            System.out.println("♕ 240 Chess Server: " + piece);
            DatabaseManager.createDatabase();
    }
}
import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        //create server object
        Server server = new Server();
        //start server on port 8080
        int port = 8000;
        int actualPort = server.run(port);

        //check server start
        System.out.println("Server started on port: " + actualPort);
    }
}
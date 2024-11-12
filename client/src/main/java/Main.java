import chess.*;
//import server.Server;
import ui.ChessClient;


public class Main {
    public static void main(String[] args) {
        //start the server
//        var server = new Server();
//        var port = server.run(8081);
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        //Create and start the client
//        String serverUrl = "http://localhost:" + port;
//        ChessClient client = new ChessClient(serverUrl);
//        client.run();
    }
}
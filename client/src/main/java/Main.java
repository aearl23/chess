import chess.*;
import ui.ChessClient;


public class Main {
    public static void main(String[] args) {

        //Create and start the client
        int port = 8081;
        String serverUrl = "http://localhost:" + port;
        ChessClient client = new ChessClient(serverUrl);
        client.run();
    }
}

import java.net.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class Chatserver extends WebSocketServer 
{
    @Override
    // Called when the Server gets a new client connection.
    public void onOpen(WebSocket ws, ClientHandshake ch) {
    }

    @Override
    // Called when a client's connection times out.
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
    }

    @Override
    // Called when we recieve data from a client.
    public void onMessage(WebSocket ws, String string) {
    }

    @Override
    // Self-explanatory.
    public void onError(WebSocket ws, Exception e) {
        e.printStackTrace();
    }
    
    @Override
    // Called on start up.
    public void onStart() {
        System.out.println( "The chat server has been started." );
    }
    
    public Chatserver(String hostname, int port) {
        super( new InetSocketAddress( hostname, port ) );
    }
    
    public static void main( String[] args )
    { 
        Chatserver server = new Chatserver( "localhost", 8000 );
        server.run();
    }
}

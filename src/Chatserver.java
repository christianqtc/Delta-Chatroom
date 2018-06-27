
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Chatserver
{
    private final ServerSocket gServerSocket;
    
    private final List<Client> gClients = new ArrayList<>();
    protected static final Logger gLogger = Logger.getLogger("com.cs4310delta");
    
    public void onStart()
    {
        System.out.println( "The server has been started." );
    }
    
    public synchronized void onOpen( Client client )
    {
        gClients.add( client );
        
        client.log( "Connected to the server." );
    }
    
    public synchronized void onMessage( Client client, String data )
    {
        System.out.println( "Recieved message: " + data );
    }
    
    public synchronized void onClose( Client client, int statusCode, String reason )
    {
        client.gThread.interrupt(); // Stop the client thread.
        gClients.remove( client );
        
        client.log( "Disconnected from the server. Reason: " + reason + "(" + statusCode + ")" );
    }
    
    public Client findClientByIP( String address )
    {
        try
        {
            InetAddress addr = InetAddress.getByName( address );
            return findClientByIP( addr );
        }
        catch (UnknownHostException e )
        {
            // Silent.
            return null;
        }
    }
    
    public Client findClientByIP( InetAddress address )
    {
        String addr = address.getHostAddress();
        for( Client client : gClients )
        {
            if ( client.gAddress.equals(addr) )
                return client;
        }
        return null;
    }
    
    public Chatserver( InetSocketAddress address ) throws IOException
    {
        gServerSocket = new ServerSocket( address.getPort(), 0, address.getAddress() );
    }
    
    public void run() throws IOException
    {
        onStart();
        
        while ( true )
        {
            Socket clientSocket = gServerSocket.accept();
            
            if ( findClientByIP( clientSocket.getInetAddress() ) != null )
            {
                // The client is already connected.
                clientSocket.close();
                continue;
            }
            
            Client client = new Client( this, clientSocket );
            
            Thread clientWorkerThread = new Thread()
            {
                @Override
                public void run()
                {
                    // The WebSocket handshake must be done first.
                    if ( !client.ws_handshake() )
                    {
                        client.disconnect( "WS handshake failed." );
                    }
                    else
                    {
                        onOpen( client );
                        
                        while ( true )
                        {
                            try
                            {
                                String data = client.ws_readFrame();
                                onMessage( client, data );
                            }
                            catch ( SocketException e )
                            {
                                // Silent.
                            }
                            catch ( Exception e )
                            {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                }
            };
            
            // If set to true, this Thread will automatically exit when the
            // main Thread stops.
            clientWorkerThread.setDaemon(true);
            
            // Start the client worker thread.
            client.gThread = clientWorkerThread;
            clientWorkerThread.start();
        }
    }
    
    public static void main( String[] args )
    { 
        try
        {
            Chatserver server = new Chatserver( new InetSocketAddress( "localhost", 8000 ) );
            server.run();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}

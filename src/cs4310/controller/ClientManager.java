package cs4310.controller;

import cs4310.model.Identify;
import cs4310.model.LoginPack;
import cs4310.model.LoginResultPack;
import cs4310.model.Message;
import cs4310.model.MessagePack;
import cs4310.model.RegistrationPack;
import cs4310.model.User;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientManager
{
    private final ServerSocket gServerSocket;
    
    private final List<Client> gClients = new ArrayList<>();
    protected static final Logger LOGGER = Logger.getLogger("com.cs4310delta");
    
    static
    {
        // For debugging
        LOGGER.setLevel(Level.FINEST);
        
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        LOGGER.addHandler(handler);
    }
    
    private void onStart()
    {
        System.out.println( "ClientManager started" );
    }
    
    synchronized void onOpen( Client client )
    {
        gClients.add( client );
        
        client.log( "Connected to the server." );
    }
    
    synchronized void onMessage( Client client, String data )
    {
        System.out.println( "Recieved message: " + data );
        
        String type = Identify.type(data);
        switch( type )
        {
            case "login":
            {
                if ( client.getUserModel() == null )
                {
                    // The Client hasn't been authenticated yet.
                    LoginPack packet = new LoginPack( data );
                    if ( packet.userName != null && packet.password != null )
                    {
                        // Try to find the user in the Database.
                        User user = new User( packet.userName );
                        if ( user.userName != null && user.password.equals(packet.password) )
                        {
                            // Found it.
                            client.setUserModel( user );
                            
                            // Send success response.
                            LoginResultPack responsePacket = new LoginResultPack( true );
                            client.send( responsePacket.toJson() );
                            break;
                        }
                    }
                }
                
                // Send failed response.
                LoginResultPack responsePacket = new LoginResultPack( false );
                client.send( responsePacket.toJson() );
                
                break;
            }
            case "message":
            {
                if ( client.getUserModel() != null ) // Always check if client is authenticated.
                {
                    MessagePack packet = new MessagePack( data );
                    if ( packet.author != null && packet.message != null && packet.message.length() > 0 )
                    {
                        final String json = packet.toJson();
                        gClients.forEach((c) -> {
                            c.send( json );
                        });
                    }
                }
                
                break;
            }
            case "registration":
            {
                RegistrationPack packet = new RegistrationPack( data );
                if ( packet.userName != null && packet.userName.length() > 0 && 
                        packet.password != null && packet.password.length() > 0 )
                {
                    User query = new User( packet.userName );
                    if ( query.userName == null ) // first check if user exists in DB
                    {
                        // Make a new User model and add to the database.
                        User user = new User( packet );
                        user.addToDB();
                        client.setUserModel(user);
                        
                        // Send success response.
                        LoginResultPack responsePacket = new LoginResultPack( true );
                        client.send( responsePacket.toJson() );
                        break;
                    }
                    else
                    {
                        if ( query.password.equals( packet.password ) )
                        {
                            // TODO: Edit details in database.
                            User.removeFromDB(packet.userName);
                            User user = new User( packet );
                            user.addToDB();
                            client.setUserModel(user);
                            
                            // Send success response.
                            LoginResultPack responsePacket = new LoginResultPack( true );
                            client.send( responsePacket.toJson() );
                            break;
                        }
                    }
                }
                
                // Send failed response.
                LoginResultPack responsePacket = new LoginResultPack( false );
                client.send( responsePacket.toJson() );
                break;
            }
            case "messagerequest":
            {
                Message[] messages = Message.history(0, 49);
                Stack<Message> stack = new Stack<>();
                for ( Message m : messages )
                {
                    if ( m.author == null )
                        break;
                    
                    stack.push(m);
                }
                
                while ( !stack.empty() )
                {
                    Message message = stack.pop();
                    client.send( message.toJson() );
                }
            }
        }
    }
    
    synchronized void onClose( Client client, int statusCode, String reason )
    {
        client.gThread.interrupt(); // Stop the client thread.
        gClients.remove( client );
        
        client.log( "Disconnected from the server. Reason: " + reason + "(" + statusCode + ")" );
        client.setUserModel(null);
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
        for( Client client : gClients )
        {
            if ( client.getAddress().equals(address) )
                return client;
        }
        return null;
    }
    
    public ClientManager( InetSocketAddress address ) throws IOException
    {
        gServerSocket = new ServerSocket( address.getPort(), 0, address.getAddress() );
    }
    
    public void run()
    {
        onStart();
        
        while ( true )
        {
            Socket clientSocket;
            try
            {
                clientSocket = gServerSocket.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                break;
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
                                String data = client.ws_readDataFrame();
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
            // main application Thread stops.
            clientWorkerThread.setDaemon(true);
            
            // Start the client worker thread.
            client.gThread = clientWorkerThread;
            clientWorkerThread.start();
        }
    }
}

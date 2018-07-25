/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4310;

import java.io.IOException;
import java.net.InetSocketAddress;
import cs4310.controller.ClientManager;

/**
 *
 * @author nikol
 */
public class Main 
{
    public static void main( String[] args )
    {
        String hostName;
        if ( args.length >= 1 )
            hostName = args[0];
        else
            hostName = "localhost";
        
        try
        {
            ClientManager cm = new ClientManager( new InetSocketAddress( hostName, 8000 ) );
            cm.run();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        
        System.out.println( "The chatserver has been started." );
    }
}

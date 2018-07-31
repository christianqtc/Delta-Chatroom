/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs4310;

import java.io.IOException;
import java.net.InetSocketAddress;
import cs4310.controller.ClientManager;
import cs4310.controller.PageServicer;
import java.util.Scanner;

/**
 *
 * @author nikol
 */
public class Main 
{
    static Thread cmThread;
    static Thread psThread;
    
    public static void main( String[] args )
    {
        String hostName;
        if ( args.length >= 1 )
            hostName = args[0];
        else
            hostName = "localhost";
        
        // Initialize ClientManager module.
        ClientManager cm;
        try
        {
            cm = new ClientManager( new InetSocketAddress( hostName, 8000 ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return;
        }
        
        cmThread = new Thread() { 
            @Override
            public void run() {
                cm.run();
            }
        };
        
        cmThread.setDaemon(true);
        
        psThread = new Thread() {
            @Override
            public void run() {
                PageServicer ps = new PageServicer( new InetSocketAddress( hostName, 80 ) );
            }
        };
        
        psThread.setDaemon(true);
        
        // Start the threads.
        cmThread.start();
        psThread.start();

        Scanner in = new Scanner( System.in );
        
        System.out.println( "The chatserver has been started." );
        
        while ( true )
        {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if ( line.equalsIgnoreCase( "poweroff" ) ) {
                break;
            }
        }
    }
}

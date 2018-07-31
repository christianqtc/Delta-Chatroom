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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikol
 */
public class Main 
{
    static Thread cmThread;
    static Thread psThread;
    
    public static final Logger LOGGER = Logger.getLogger("com.cs4310delta");
    private static boolean usingSrcAsCWD = true;
    
    public static boolean isUsingSrcFolderAsCWD() { return usingSrcAsCWD; }
    
    public static void main( String[] args )
    {
        // For debugging
        LOGGER.setLevel(Level.INFO);
        
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        LOGGER.addHandler(handler);
        
        // Hostname
        String hostName;
        if ( args.length >= 1 )
            hostName = args[0];
        else
            hostName = "localhost";
        
        // CWD setting
        if ( args.length >= 2 )
        {
            if ( args[1] != null && args[1].length() > 0 )
            {
                try
                {
                    int value = Integer.parseInt( args[1] );
                    if ( value == 0 )
                        usingSrcAsCWD = false;
                }
                catch ( NumberFormatException e )
                {
                    // Don't do anything. Assume true.
                }
            }
        }
        
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
        
        // Initialize PageServicer module.
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
        
        // Admin command loop.
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

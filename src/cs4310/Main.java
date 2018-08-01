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
import java.util.ArrayList;
import java.util.List;
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
        
        boolean running = true;
        
        // Admin command loop.
        while ( running )
        {
            System.out.print("> ");
            String line = in.nextLine().trim();
            
            Scanner sLine = new Scanner( line );
            List<String> cmdArgs = new ArrayList<>();
            while ( sLine.hasNext() )
                cmdArgs.add(sLine.next());
            
            if ( cmdArgs.size() > 0 )
            {
                String cmdName = cmdArgs.get(0).toLowerCase();
                
                switch ( cmdName )
                {
                    case "poweroff":
                    {
                        running = false;
                        break;
                    }
                    case "log_setlevel":
                    {
                        if ( cmdArgs.size() >= 2 )
                        {
                            try {
                                Level logLevel = Level.parse( cmdArgs.get(1) );
                                LOGGER.setLevel(logLevel);
                                System.out.println( "Log level set to " + logLevel.toString() );
                            } catch ( IllegalArgumentException e )
                            {
                                System.out.println( "Invalid log level specified" );
                            }
                        }
                    }
                }
            }
        }
    }
}

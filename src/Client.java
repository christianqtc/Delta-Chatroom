import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Client 
{
    public final String gAddress;
    final Chatserver gServer;
    final Socket gSocket;
    Thread gThread;
    
    public Client( Chatserver server, Socket skt )
    {
        gServer = server;
        gSocket = skt;
        
        gAddress = gSocket.getInetAddress().getHostAddress();
        
        ws_init();
    }
    
    public void send( String data )
    {
        ws_sendDataFrame( FRAME_OPCODE_TEXT, data.getBytes() );
    }
    
    // Forcibly disconnect this client from the Server with a reason.
    public void disconnect( String reason )
    {
        disconnect( 0x1000, reason );
    }  
    
    public void disconnect( int statusCode, String reason )
    {
        // Since we're the one sending the close signal, there's no need
        // to echo the client's response to this data frame.
        if ( g_wsEchoCloseSignal )
        {
            g_wsEchoCloseSignal = false;
            
            char[] chars = reason.toCharArray();
            byte[] payload = new byte[ chars.length + 2 ];
            payload[0] = (byte)((statusCode & 0xFF00) >> 8); 
            payload[1] = (byte)(statusCode & 0xFF);
            for ( int i = 0; i < chars.length; i++ )
                payload[2 + i] = (byte)chars[i];
            
            ws_sendDataFrame( FRAME_OPCODE_CLOSE, payload );
        }
    }
    
    public void log( String msg )
    {
        log( Level.INFO, msg );
    }
    
    public void log( Level level, String msg )
    {
        gServer.gLogger.log( level, "{0}: {1}", new Object[] { gAddress, msg }  );
    }
    
    /*
        ======================================
        ======================================
             WEBSOCKET PROTOCOL INTERNALS
        ======================================
        ======================================
    */
    
    // Important thing to note: WebSocket processes multi-byte data in the
    // data frame in network byte order, or most significant first. That means
    // in, say, a long being transmitted:
    // 87 12 F4 23 15 12 92 01
    // 87 is transferred first. Then 12, then F4, etc.
    
    // General opcodes
    private static final byte FRAME_OPCODE_TEXT = 0x1;
    
    // Control opcodes
    private static final byte FRAME_OPCODE_CLOSE = 0x8;
    private static final byte FRAME_OPCODE_PING = 0x9;
    private static final byte FRAME_OPCODE_PONG = 0xA;
    
    private boolean g_wsEchoCloseSignal;
    
    private void ws_init()
    {
        g_wsEchoCloseSignal = true;
    }
    
    // Perform the WebSocket handshake. True if successful, false otherwise.
    protected boolean ws_handshake()
    {
        assert( gSocket != null );
        
        try
        {
            Map<String, String> clientHeader = new HashMap<>();
            
            PrintWriter out = new PrintWriter( gSocket.getOutputStream() );

            // Listen for client's initiation via GET request.
            {
                // Wait for only 5 seconds.
                gSocket.setSoTimeout(5000);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int b; int e = 0; boolean b1 = false; boolean b2 = false;
                while ( ( b = gSocket.getInputStream().read() ) > 0 )
                {
                    if ( b1 && b2 )
                        e++;
                    if ( e == 2 )
                        break;

                    // Check for a sequence of '\r\n\r\n'. 
                    // Need this to detect when the client's handshake data ends.
                    switch (b) {
                        case '\r':
                            b1 = true;
                            break;
                        case '\n':
                            if ( b1 ) 
                            {
                                b2 = true;
                                break;
                            }
                        default:
                            b1 = false;
                            b2 = false;
                            e = 0;
                            break;
                    }
                    
                    buffer.write(b);
                }
                
                buffer.flush();

                gSocket.setSoTimeout(0);

                // Store header data into clientHeader.
                String data = buffer.toString();
                for ( String line : data.split("\r\n") )
                {
                    String[] split = line.split(":", 2);
                    if ( split.length > 0 )
                    {
                        String k = split[0].trim();
                        if ( k.length() > 0 )
                        {
                            String v = null;
                            if ( split.length > 1 )
                                v = split[1].trim();

                            clientHeader.put(k, v);
                        }
                    }
                }
            }
            
            // Ensure the client's key is present.
            String clientKey = clientHeader.get( "Sec-WebSocket-Key" );
            if ( clientKey == null )
                return false;

            // Generate the server key.
            String serverKey;
            {
                String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

                // Hash with SHA-1
                MessageDigest sha1 = MessageDigest.getInstance( "SHA-1" );
                byte[] result = sha1.digest(( clientKey + magicString ).getBytes());

                // Encode the hash with Base64 to yield the final server key.
                serverKey = Base64.getEncoder().encodeToString( result );
            }

            // Finally, send the response to the client.
            out.print( "HTTP/1.1 101 Switching Protocols\r\n" );
            out.print( "Upgrade: websocket\r\n");
            out.print( "Connection: upgrade\r\n" );
            out.print( "Sec-WebSocket-Accept: " + serverKey + "\r\n" );
            out.print( "\r\n" ); // mark the end of the response
            out.flush(); // send it.
        }
        catch ( SocketTimeoutException e )
        {
            return false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private static final int FRAME_TYPEINFO = 0;
    private static final int FRAME_PAYLOADINFO = 1;
    private static final int FRAME_EXTPAYLOADINFO = 2;
    private static final int FRAME_MASK = 3;
    private static final int FRAME_PAYLOAD = 4;
    
    protected void ws_sendDataFrame( byte opCode, byte[] payload )
    {
        ws_sendDataFrame( opCode, payload, true, false, 0 );
    }
    
    protected void ws_sendDataFrame( byte opCode, byte[] payload, boolean lastFrame )
    {
        ws_sendDataFrame( opCode, payload, lastFrame, false, 0 );
    }
    
    protected void ws_sendDataFrame( byte opCode, byte[] payload, boolean lastFrame, boolean masked, int mask ) 
    {
        if ( gSocket.isClosed() )
            return;
        
        try
        {
            OutputStream out = gSocket.getOutputStream();
            
            byte typeInfo = 0;
            if ( lastFrame )
                typeInfo |= 0x80;
            typeInfo |= ( opCode & 0x0F );
            out.write( Byte.toUnsignedInt(typeInfo) );

            byte payloadInfo = 0;
            if ( masked )
                payloadInfo |= 0x80;

            long payloadLen = Integer.toUnsignedLong( payload.length );
            int extPayloadBytesLen = 0;
            
            if ( payload.length <= 125 )
                payloadInfo |= (byte)payload.length;
            else if ( payload.length <= 0xFFFF )
            {
                payloadInfo |= (byte)126;
                extPayloadBytesLen = 2;
            }
            else
            {
                payloadInfo |= (byte)127;
                extPayloadBytesLen = 8;
            }
            
            out.write( Byte.toUnsignedInt(payloadInfo) );

            if ( extPayloadBytesLen == 2 )
            {
                out.write( (int)(( payloadLen & 0xFF00 ) >>> 8 ) );
                out.write( (int)( payloadLen & 0xFF ) );
            }
            else if ( extPayloadBytesLen == 8 )
            {
                // If only unsigned long existed on Java...
                out.write( (int)(( payloadLen & 0xFF00000000000000L ) >>> 56 ) );
                out.write( (int)(( payloadLen & 0xFF000000000000L ) >>> 48 ) );
                out.write( (int)(( payloadLen & 0xFF0000000000L ) >>> 40 ) );
                out.write( (int)(( payloadLen & 0xFF00000000L ) >>> 32 ) );
                out.write( (int)(( payloadLen & 0xFF000000L ) >>> 24 ) );
                out.write( (int)(( payloadLen & 0xFF0000L ) >>> 16 ) );
                out.write( (int)(( payloadLen & 0xFF00L ) >>> 8 ) );
                out.write( (int)( payloadLen & 0xFFL ) );
            }

            if ( masked )
            {
                out.write( (int)(( mask & 0xFF000000 )) >>> 24 );
                out.write( (int)(( mask & 0xFF0000 )) >>> 16 );
                out.write( (int)(( mask & 0xFF00 )) >>> 8 );
                out.write( (int)( mask & 0xFFL ) );
            }

            out.write(payload);

            out.flush();
        }
        catch ( SocketException e )
        {
            // Don't do anything.
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    protected String ws_readFrame() throws IOException
    {
        ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream();
        
        boolean lastFrame = false;
        byte opCode = 0;
        
        while ( !lastFrame && !gSocket.isClosed() )
        {
            int frameReadState = FRAME_TYPEINFO;
            
            boolean masked = false;
            long payloadLen = 0L; byte readBytePos = 0; long bytesToRead = 0L;
            long maskKey = 0L;
            
            // Process incoming frames, until we hit the last marked frame.
            OUTER:
            for (long frameBytePos = 0;; frameBytePos++) 
            {
                int _b = gSocket.getInputStream().read();
                if ( _b <= 0 )
                    break;
                
                byte b = (byte)(_b);
                
                if ( frameBytePos == 0 )
                {
                    log( Level.FINEST, "---FRAME START---" );
                }
                
                log( Level.FINEST, String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b)) ).replace(' ', '0') );
                
                switch (frameReadState) 
                {
                    case FRAME_TYPEINFO:
                    {
                        lastFrame = ((b >>> 7) & 0xFF) != 0;
                        byte _opCode = (byte)(( b & 0x0F ));
                        if ( opCode == 0 )
                            opCode = _opCode;
                        
                        // If opcode is a control opcode, ALWAYS is last frame.
                        if ( ( opCode & 0x8 ) != 0 )
                            lastFrame = true;
                        
                        log( Level.FINEST, "FIN: " + lastFrame );
                        log( Level.FINEST, "OPCODE: " + opCode );
                        
                        frameReadState = FRAME_PAYLOADINFO;
                        break;
                    }
                    case FRAME_PAYLOADINFO:
                    {
                        masked = (b >>> 7) != 0;
                        log( Level.FINEST, "MASKED: " + masked );
                        payloadLen = Byte.toUnsignedInt(b) & 0x7F;
                        
                        if ( payloadLen <= 125 )
                        {
                            log( Level.FINEST, "PAYLOAD LENGTH: " + payloadLen );
                            
                            if ( masked )
                            {
                                frameReadState = FRAME_MASK;
                                readBytePos = 0;
                                bytesToRead = 4;
                            }
                            else
                            {
                                frameReadState = FRAME_PAYLOAD;
                                readBytePos = 0;
                                bytesToRead = payloadLen;
                            }
                        }
                        else if ( payloadLen == 126 )
                        {
                            frameReadState = FRAME_EXTPAYLOADINFO;
                            readBytePos = 0;
                            bytesToRead = 2; // read next 2 bytes for payload length
                        }
                        else if ( payloadLen == 127 )
                        {
                            frameReadState = FRAME_EXTPAYLOADINFO;
                            readBytePos = 0;
                            bytesToRead = 8; // read next 8 bytes for payload length
                        }   
                        break;
                    }
                    case FRAME_EXTPAYLOADINFO:
                    {
                        payloadLen <<= 8;
                        payloadLen |= Byte.toUnsignedInt(b);
                        
                        readBytePos++;
                        if ( readBytePos == bytesToRead )
                        {
                            log( Level.FINEST, "EXTENDED PLEN: " + payloadLen );
                            
                            if ( masked )
                            {
                                frameReadState = FRAME_MASK;
                                readBytePos = 0;
                                bytesToRead = 4;
                            }
                            else
                            {
                                frameReadState = FRAME_PAYLOAD;
                                readBytePos = 0;
                                bytesToRead = payloadLen;
                            }
                        }
                        break;
                    }
                    case FRAME_MASK:
                    {
                        maskKey |= ( Byte.toUnsignedInt(b) << ( 8 * readBytePos ) );
                        
                        readBytePos++;
                        if ( readBytePos == bytesToRead )
                        {
                            log( Level.FINEST, "MASK: " + String.format("%"+bytesToRead*8L+"s", Long.toBinaryString( maskKey ) ).replace(' ', '0') );
                            
                            frameReadState = FRAME_PAYLOAD;
                            readBytePos = 0;
                            bytesToRead = payloadLen;
                        }   
                        
                        break;
                    }
                    case FRAME_PAYLOAD:
                    {
                        byte d = b;
                        if ( masked )
                        {
                            byte maskByte = (byte)( maskKey >>> ( ( readBytePos % 4 ) * 8 ) );
                            d ^= maskByte;
                        }
                        
                        payloadBuffer.write(Byte.toUnsignedInt(d));
                        
                        readBytePos++;
                        if (readBytePos == bytesToRead) 
                        {
                            payloadBuffer.flush();
                            
                            log( Level.FINEST, "----FRAME END----" );
                            break OUTER;
                        }
                        
                        break;
                    }
                }
                
                log( Level.FINEST, "-----------------");
            }
            
            if ( lastFrame )
            {
                switch ( opCode )
                {
                    case FRAME_OPCODE_CLOSE:
                    {
                        // Close
                        byte[] payload = payloadBuffer.toByteArray();
                        if ( g_wsEchoCloseSignal )
                        {
                            // Standard procedure is to echo.
                            g_wsEchoCloseSignal = false;
                            ws_sendDataFrame( FRAME_OPCODE_CLOSE, payload );
                        }
                        
                        // In a Close data frame, the first two bytes of the payload is a status code.
                        int statusCode = ( Byte.toUnsignedInt(payload[0]) << 8 ) | Byte.toUnsignedInt( payload[1] );
                        
                        // Flush.
                        lastFrame = false;
                        opCode = 0;
                        payloadBuffer.reset();
                        
                        // Tell the server we've disconnected.
                        gServer.onClose( this, statusCode, payload.length > 2 ? payloadBuffer.toString().substring(2) : "" );
                        
                        break;
                    }
                    case FRAME_OPCODE_PING:
                    {
                        // Standard procedure is to echo wtih a Pong data frame.
                        byte[] payload = payloadBuffer.toByteArray();
                        ws_sendDataFrame( FRAME_OPCODE_PONG, payload );

                        // Flush.
                        lastFrame = false;
                        opCode = 0;
                        payloadBuffer.reset();
                        
                        break;
                    }
                    case FRAME_OPCODE_PONG:
                    {
                        // Flush. Ignore it.
                        lastFrame = false;
                        opCode = 0;
                        payloadBuffer.reset();
                        break;
                    }
                }
            }
        }
        
        // If control flow reaches here, it's a message.
        return payloadBuffer.toString();
    }
}
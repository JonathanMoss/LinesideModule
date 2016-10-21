
package com.jgm.lineside.interlocking;

import static com.jgm.lineside.LineSideModule.ExitCommandLine;
import static com.jgm.lineside.LineSideModule.dataLogger;
import static com.jgm.lineside.LineSideModule.getFailed;
import static com.jgm.lineside.LineSideModule.getOK;
import com.jgm.lineside.datalogger.Colour;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;


/**
 * This class provides the functionality to connect to the Remote Interlocking Server.
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class RemoteInterlockingClient extends Thread {
    
    private String remoteInterlockingIP;
    private int remoteInterlockingPort;
    private SocketAddress sockAddress;
    private Socket conn; // The connection object.
    
    /**
     * This is the constructor method for the RemoteInterlockingClient
     * @param remoteInterlockingIP <code>String</code> containing the IP Address of the Remote Interlocking.
     * @param remoteInterlockingPort <code>Integer</code> The port number that the Remote Interlocking is listening for connections on.
     */
    public RemoteInterlockingClient(String remoteInterlockingIP, int remoteInterlockingPort) {
        
        this.remoteInterlockingIP = remoteInterlockingIP;
        this.remoteInterlockingPort = remoteInterlockingPort;

        // Create a SockAddress object.
        try {
            this.sockAddress = new InetSocketAddress(InetAddress.getByName(this.remoteInterlockingIP), this.remoteInterlockingPort);
        } catch (UnknownHostException ex) {
            dataLogger.sendToDataLogger (String.format ("%s%s: Invalid IP Address.%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                    true, true);
            ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
        }
    }
    
    // This method sets up the Input and Output Stream Thread objects.
    private void setUpStreams() {
        
        try {
            new Thread(new IncomingMessage(this.conn.getInputStream())).start();
            new Thread(new OutgoingMessage(this.conn.getOutputStream())).start();
            dataLogger.sendToDataLogger(String.format ("%s%s%s",
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()), 
                true, true);
        } catch (IOException ex) {
            dataLogger.sendToDataLogger (String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                    true, true);
            ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()));
        }
        
    }
    
    // This Method establishes a connection with the Remote Interlocking.
    private void connectToServer() {
        if (this.conn == null || !this.conn.isConnected()) {
            try {
                this.conn = new Socket(); // Create a new Socket Object.
                this.conn.connect(sockAddress, 10000); // Attempt a connection.
                MessageHandler.setConnectionToRemoteInterlocking(this.conn); // Register the connection on the MessageHandler.
            } catch (IllegalArgumentException iae) {
                dataLogger.sendToDataLogger (String.format ("%s%s: Invalid IP Address.%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                        true, true);
                ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            } catch (IOException e) {
                dataLogger.sendToDataLogger (String.format ("%s%s: Cannot find the Remote Interlocking Server.%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()),
                        true, true);
                ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            }
        }
    }
    
    @Override
    public void run() {
        
        this.connectToServer();
        this.setUpStreams();
        
    }

}

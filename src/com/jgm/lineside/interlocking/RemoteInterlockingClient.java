
package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import com.jgm.lineside.datalogger.Colour;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the functionality to connect to the Remote Interlocking Server.
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class RemoteInterlockingClient extends Thread {
    private static String remoteInterlockingIP;
    private static int remoteInterlockingPort;
    private static String LinesideModuleID;
    public OutgoingMessages outgoing;
    private SocketAddress sockAddress;
    private Socket conn; // The connection object.
    
    /**
     * This is the constructor method for the RemoteInterlockingClient
     * @param remoteInterlockingIP
     * @param remoteInterlockingPort
     * @param linesideModuleID 
     */
    public RemoteInterlockingClient(String remoteInterlockingIP, int remoteInterlockingPort, String linesideModuleID) {
        RemoteInterlockingClient.remoteInterlockingIP = remoteInterlockingIP;
        RemoteInterlockingClient.remoteInterlockingPort = remoteInterlockingPort;
        RemoteInterlockingClient.LinesideModuleID = linesideModuleID;
        try {
            this.sockAddress = new InetSocketAddress(InetAddress.getByName(RemoteInterlockingClient.remoteInterlockingIP), RemoteInterlockingClient.remoteInterlockingPort);
        } catch (UnknownHostException ex) {
            try {
                LineSideModule.dataLogger.sendToDataLogger (String.format ("%s%s: Invalid IP Address.%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()),
                    true, true);
                LineSideModule.ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            } catch (IOException ex1) {}
        }
    }
    private void setUpStreams() {
        if (this.conn != null & this.conn.isConnected()) {
            try {
                new Thread(new IncomingMessages(this.conn.getInputStream())).start();
                new Thread(this.outgoing = new OutgoingMessages(this.conn.getOutputStream())).start();
            } catch (IOException ex) {
                Logger.getLogger(RemoteInterlockingClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void connectToServer() {
        if (this.conn == null || !this.conn.isConnected()) {
            try {
                this.conn = new Socket();
                this.conn.connect(sockAddress, 10000);   
            } catch (IllegalArgumentException iae) {
                try {
                    LineSideModule.dataLogger.sendToDataLogger (String.format ("%s%s: Invalid IP Address.%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()),
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                } catch (IOException ex) {}    
            } catch (IOException e) {
                try {
                    LineSideModule.dataLogger.sendToDataLogger (String.format ("%s%s: Cannot find the Remote Interlocking Server.%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()),
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERROR: Cannot connect to the remote interlocking.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                } catch (IOException ex) {}
            }
        }
    }
    
    @Override
    public void run() {
        this.connectToServer();
        this.setUpStreams();
        this.outgoing.sendMessageToRemoteInterlocking("HELLO");
        this.outgoing.sendMessageToRemoteInterlocking("HELLO2");
        
    }

}

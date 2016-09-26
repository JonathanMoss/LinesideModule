package com.jgm.lineside;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * This class provides the functionality to connect to the Data Logger
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class DataLoggerClient extends Thread {
    private final String dataLoggerIP; // The IP address of the Data Logger
    private final int dataLoggerPort; // The port number of the Data Logger
    private DataOutputStream output; // The Socket output stream
    private Socket conn; // The connection object
    private volatile Boolean connected = true; // A flag that indicates if connected - used to keep the thread live
    private final String lsmIdentity; // The identity of the Line Side Module where a DataLoggerClient is instantiated
    private SocketAddress sockAddress; // The IP/Portnumber socketAddress used in the connection string
    private DataInputStream input; // The input stream - used only to determine if the connection to the Data Logger remains live.
    
    /**
     * This is the Constructor Method for the DataLoggerClient class
     * @param dataLoggerIP A <code>String</code> containing the IP address of the Data Logger.
     * @param dataLoggerPort An <code>int</code> containing the listening port number of the Data Logger.
     * @param lsmIdentity A <code>String</code> that details the Lineside Module identity.
     * @throws IOException 
     */
    protected DataLoggerClient(String dataLoggerIP, int dataLoggerPort, String lsmIdentity) throws IOException {
        this.dataLoggerIP = dataLoggerIP;
        this.dataLoggerPort = dataLoggerPort;
        this.lsmIdentity = lsmIdentity;
    }
    
    // This method attempts to establish a connection with the server.
    private void connectToServer() throws IOException {
        if (this.conn == null) {
            this.sendToDataLogger("Attempting a connection to the Data Logger...",true, false);
            this.sockAddress = new InetSocketAddress (InetAddress.getByName(this.dataLoggerIP), this.dataLoggerPort); 
            this.conn = new Socket();
            this.conn.connect(sockAddress, 10000);
            this.connected = true;
            this.sendToDataLogger(String.format("OK %s", this.conn.toString()), true, true);
        }
    }
    
    // This method attempts to setup the output and input streams
    private void setupStreams() throws IOException {
        if (this.conn != null) {
            this.output  = new DataOutputStream(this.conn.getOutputStream());
            this.output.writeUTF(this.lsmIdentity);
            this.output.flush();
            this.input = new DataInputStream(this.conn.getInputStream());
        }
    }
    
    // This method is called only when something goes wrong - it makes sure the connection object and input/output streams are closed.
    private void closeConnection() throws IOException, NullPointerException {
        this.conn.close();
        this.output.close();
        this.input.close();
        this.input = null;
        this.conn = null;
        this.output = null;
    }
    
    // This method ensures that the thread remains live whilst a valid connection to the DataLogger exists.
    private void whileConnected() throws IOException {
        while (this.connected) {
            this.input.readUTF(); // This checks if there is still a connection with the server.
        }
    }
    
    /**
     * This method sends a message to the DataLogger
     * 
     * @param message A <code>String</code> representing the message to send to the DataLogger if a valid connection exists.
     * @param console A <code>Boolean</code> value, <i>true</i> prints the output of the message to the console, otherwise <i>false</i>. 
     * @param carriageReturn A <code>Boolean</code> value, <i>true</i> appends a carriage return to the end of the message, otherwise <i>false</i>.
     * @throws IOException 
     */
    public synchronized void sendToDataLogger (String message, Boolean console, Boolean carriageReturn) throws IOException, NullPointerException{
        if (console) { // If console is set to true, display message on the console.
            System.out.print(String.format("%s%s",message, (carriageReturn) ? "\n" : "" ));
        }
        if (this.connected && this.output != null) { // Check connected, if so - send message to the DataLogger.
            this.output.writeUTF(String.format("LSM|%s|%s", this.lsmIdentity, message));
            this.output.flush(); 
        }
    }
    
    // This method overrides the run() method from the Thread Class.
    @Override
    public void run() {
        try {
            this.connectToServer();
            this.setupStreams();
            this.whileConnected();
            // There is no valid connection to send the message to.
        } catch (EOFException e) {
            try {
                // The connection with the DataLogger has terminated.
                this.sendToDataLogger("WARNING: The Data Logger has terminated the connection." ,true,true);
            } catch (IOException ex) {}
        } catch (ConnectException con) {
            try {
                // Cannot create a connection with the data logger.
                this.sendToDataLogger(String.format("FAILED\nWARNING: Cannot connect to the Data Logger(Connection Refused) [%s:%s]", 
                    this.dataLoggerIP, this.dataLoggerPort),true,true);
            } catch (IOException | NullPointerException ex) {}
        } catch (IOException e) {
        } finally {
            //this.connected = false;
            try {
                this.closeConnection();
            } catch (IOException | NullPointerException ex) {}
        }
    }
}

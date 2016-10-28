package com.jgm.lineside.datalogger;

import static com.jgm.lineside.ApplicationUtilities.getFailed;
import static com.jgm.lineside.ApplicationUtilities.getNewLine;
import static com.jgm.lineside.ApplicationUtilities.getOK;
import com.jgm.lineside.LineSideModule;
import customexceptions.DataLoggerException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * This class provides a DataLogger Connection.
 * <p>
 * 
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class DataLoggerClient extends Thread {
    
    /**
     * The IP address of the Data Logger.
     */
    private String dataLoggerIP;
    
    /**
     * The port number of the Data Logger.
     */
    private int dataLoggerPort;
    
    /**
     * The Socket output stream.
     */
    private DataOutputStream output;
    
    /**
     * The connection object.
     */
    private Socket conn;
    
    /**
     * The identity of the module that requires a connection to the DataLogger.
     */
    private String moduleIdentity; 
    
    /**
     * The IP/Port number socketAddress used in the connection string.
     */
    private SocketAddress sockAddress;
    
    /**
     * The input stream - used only to determine if the connection to the Data Logger remains live.
     */
    private DataInputStream input;
    
    /**
     * A variable to hold the connection attempts.
     */
    private int connectionAttempts = 0;
    
    /**
     * A variable to hold the Connection Status of the DataLoggerClient.
     */
    private ConnectionStatus connectionStatus = ConnectionStatus.NO_CONNECTION;
    
    /**
     * A constant that defines the maximum connection attempts.
     */
    private static final int MAX_CON_ATTEMPTS = 5;
    
    /**
     * This method sets the ConnectionStatus of the DataLoggerClient object.
     * @param connectionStatus <code>ConnectionStatus</code> A constant which informs the object the status of the connection to the DataLogger.
     */
    public synchronized void setConnectionStatus (ConnectionStatus connectionStatus) {
        
        this.connectionStatus = connectionStatus;
        
    }
    
    /**
     * This method returns the ConnectionStatus of the DataLoggerClient object.
     * @return <code>ConnectionStatus</code> A constant which informs the object the status of the connection to the DataLogger.
     */
    private synchronized ConnectionStatus getConnectionStatus() {
        return this.connectionStatus;
    }
    
    /**
     * This is the Connection Method for the DataLoggerClient Object.
     * <p>
     * @param dataLoggerIP A <code>String</code> containing the IP address of the Data Logger.
     * @param dataLoggerPort An <code>int</code> containing the listening port number of the Data Logger.
     * @param moduleIdentity A <code>String</code> that details the identity of the module that requires a connection to the DataLogger.
     * @throws customexceptions.DataLoggerException
     */
    public synchronized void DataLoggerClientConnect(String dataLoggerIP, int dataLoggerPort, String moduleIdentity) throws DataLoggerException {
        
        this.dataLoggerIP = dataLoggerIP;
        this.dataLoggerPort = dataLoggerPort;
        this.moduleIdentity = moduleIdentity;
        
        try {
            
            this.sockAddress = new InetSocketAddress (InetAddress.getByName(this.dataLoggerIP), this.dataLoggerPort);
            
        } catch (UnknownHostException ex) {
            
            throw new DataLoggerException("Unable to connect the the DataLogger");
            
        }
        
        this.setConnectionStatus(ConnectionStatus.ATTEMPTING_CONNECTION);
        
    }
    
    /**
     * This method attempts a connection to the DataLogger Server.
     * 
     * @return <code>Boolean</code> <i>'true'</i> indicates the connection was successful, otherwise <i>'false'</i>.
     * @throws IOException 
     */
    private synchronized Boolean connectToServer() {
            
        this.connectionAttempts ++;
        this.sendToDataLogger(String.format ("Attempting a connection to the DataLogger (Attempt %s/%s)...", 
            this.connectionAttempts, MAX_CON_ATTEMPTS),
            true, false);
        this.conn = new Socket();
        
        try {

            Thread.sleep(3000);
            this.conn.connect(sockAddress, 10000);

        } catch (IOException ex) {
            
            return false;

        } catch (InterruptedException ex) {}

        this.sendToDataLogger(String.format("%s%s %s%s%s", 
            Colour.GREEN.getColour(), getOK(), Colour.BLUE.getColour(), this.conn.toString(), Colour.RESET.getColour()), true, true);
        this.connectionAttempts = 0;

        return true;
        
    }
    
    /**
     * This method sets up the Input and Output Streams.
     * 
     * @throws DataLoggerException 
     */
    private synchronized void setupStreams() throws DataLoggerException {

        try {

            this.output = new DataOutputStream(this.conn.getOutputStream());
            this.output.writeUTF(this.moduleIdentity);
            this.output.flush();
            Thread t1 = new Thread (new IncomingStream (this.conn.getInputStream(), this));
            t1.setName("IncomingMessageThread");
            t1.start();
            this.setConnectionStatus(ConnectionStatus.CONNECTED);
            LineSideModule.setLookingForDataLogger(false);

        } catch (IOException ex) {

            throw new DataLoggerException ("Cannot set up INPUT and OUTPUT Streams");

        }

    }
    
    /**
     * This method closed the connection.
     */
    private synchronized void closeConnection() {
        
        try {

            this.output.close();
            this.input.close();
            this.conn.close(); 

        } catch (IOException | NullPointerException e) {
        } finally {

            this.input = null;
            this.conn = null;
            this.output = null;

        }
    }
    
    /**
     * This Method attempts to read from the Input Stream.
     * <p>
     * It is used to confirm that the connection to the DataLogger is live.
     * 
     * @throws IOException 
     */
    
    /**
     * This method sends a message to the DataLogger where a valid connection exists, and to the console.
     * 
     * @param message A <code>String</code> representing the message to send to the DataLogger if a valid connection exists.
     * @param console A <code>Boolean</code> value, <i>true</i> prints the output of the message to the console, otherwise <i>false</i>. 
     * @param carriageReturn A <code>Boolean</code> value, <i>true</i> appends a carriage return to the end of the message, otherwise <i>false</i>.
     */
    public synchronized void sendToDataLogger (String message, Boolean console, Boolean carriageReturn) {
        
        if (this.connectionStatus != ConnectionStatus.CONNECTED) { // The DataLoggerClient is not connected to the Server.
            console = true; // Always send to the console.
        }
        
        if (console) { // If console is set to true, display message on the console.
            System.out.print(String.format("%s%s", message, (carriageReturn) ? getNewLine() : ""));
        }
        
        if (this.connectionStatus == ConnectionStatus.CONNECTED && this.output != null) { 
            
            try {
                this.output.writeUTF(String.format("%s", message));
                this.output.flush(); 
            } catch (IOException ex) {}

        }
    }
  
    // This method overrides the run() method from the Thread Class.
    @Override
    public void run() {
        
        while (this.getConnectionStatus() != ConnectionStatus.CONNECTION_TERMINATED) {
            
            try {
                
                switch (this.getConnectionStatus()) {
                    
                    case NO_CONNECTION:
                        // Do Nothing.
                        break;
                        
                    case ATTEMPTING_CONNECTION:
                        if (this.connectionAttempts < MAX_CON_ATTEMPTS) {
                            
                            if (this.connectToServer()) {
                                
                                this.connectionAttempts = 0;
                                this.setConnectionStatus(ConnectionStatus.CONNECTION_SETUP);
                                
                            } else {
                                
                                throw new DataLoggerException("The DataLogger is not accepting connection requests");
                                
                            }
                            
                        } else {
                            
                            this.sendToDataLogger(String.format ("%sWARNING: No further connection attempts with the Data Logger shall be made.%s",
                                Colour.RED.getColour(), Colour.RESET.getColour()),
                                true, true);
                            this.setConnectionStatus(ConnectionStatus.CONNECTION_TERMINATED);
                            this.closeConnection();
                            LineSideModule.setLookingForDataLogger(false);
                            
                        }
                        break;
                        
                    case CONNECTION_SETUP:
                        this.setupStreams();
                        break;
                        
                    case CONNECTED:
                        //this.whileConnected();
                        break;
                }
                
            } catch (DataLoggerException ex) {
                
                this.sendToDataLogger(String.format ("%s%s",
                    Colour.RED.getColour(), getFailed()),
                    true, false);
                
                this.sendToDataLogger(String.format ("%s ['%s']%s", 
                    Colour.BLUE.getColour(), ex.getMessage(), Colour.RESET.getColour()), true, true);
                
            }
        }
        
    }
}
/**
 * This Enumeration defines the valid Connection Status constants used by the DataLoggerClient Class.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
enum ConnectionStatus {

    /**
     * This constant signifies that no connection has been requested.
     * <p>
     * The DataLoggerClient Object has been instantiated but no request to connect has yet been received.
     */
    NO_CONNECTION,
    
    /**
     * This constant signifies that a request to connect has been made.
     * <p>
     * The DataLoggerClient is attempting to establish a connection to the DataLogger.
     */
    ATTEMPTING_CONNECTION,
    
    /**
     * This constant signifies that the DataLoggerClient is connected to the Data Logger.
     */
    CONNECTED,
    
    /**
     * A previously successful connection has ended.
     */
    CONNECTION_TERMINATED,
    
    /**
     * This indicates that a connection has been made and the streams are being setup.
     */
    CONNECTION_SETUP;
}


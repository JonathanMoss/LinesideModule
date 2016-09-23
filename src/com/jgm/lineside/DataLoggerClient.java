package com.jgm.lineside;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *
 * @author Jonathan Moss
 */
public class DataLoggerClient extends Thread{
    private String dataLoggerIP;
    private int dataLoggerPort;
    private DataOutputStream output;
    private Socket conn;
    private Boolean connected = false;
    private String lsmIdentity;
    public DataLoggerClient(String dataLoggerIP, int dataLoggerPort, String lsmIdentity) throws IOException {
        this.dataLoggerIP = dataLoggerIP;
        this.dataLoggerPort = dataLoggerPort;
        this.lsmIdentity = lsmIdentity;
    }
    
    private void connectToServer() throws IOException {
        SocketAddress sockAddress = new InetSocketAddress (InetAddress.getByName(this.dataLoggerIP), this.dataLoggerPort); 
        this.conn = new Socket();
        this.conn.connect(sockAddress, 10000);
        this.connected = true;
     
    }
    
    private void setupStreams() throws IOException {
        this.output  = new DataOutputStream(this.conn.getOutputStream());
        this.output.writeUTF(this.lsmIdentity);
        this.output.flush();
    }
    
    private void whileConnected() throws IOException {
        while (this.connected) {
            
        }
        
        System.out.println("Not connected to DataLogger...");
    }
    
    public synchronized void sendToDataLogger (String message, Boolean console, Boolean cr) throws IOException{
        if (console) {
            System.out.print(String.format("%s%s",message, (cr) ? "\n" : "" ));
        }
        this.output.writeUTF(String.format("LSM|%s|%s", this.lsmIdentity, message));
        this.output.flush();
    }
    
    @Override
    public void run() {
        try {
            this.connectToServer();
            this.setupStreams();
            this.whileConnected();
        } catch (IOException e) {
            this.connected = false;
            
        }
    }
}

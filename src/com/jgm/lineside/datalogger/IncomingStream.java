package com.jgm.lineside.datalogger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This method provides a threaded DataInputStream object.
 * 
 * This is used to check that the connection to the DataLogger Server is open.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class IncomingStream extends DataInputStream implements Runnable {

    private final DataLoggerClient client;
    private Boolean stayConnected = true;
    
    public IncomingStream(InputStream in, DataLoggerClient client) {
        
        super(in);
        this.client = client;
    }

    @Override
    public void run() {
    
        while (stayConnected) {
            try {
                String msg = this.readUTF();
            } catch (IOException ex) {
                client.sendToDataLogger(String.format ("%sWARNING: The connection to the DataLogger has ended%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()), 
                    true, true);
                client.setConnectionStatus(ConnectionStatus.ATTEMPTING_CONNECTION);
                this.stayConnected = false;
                
            }
        }
    }

}

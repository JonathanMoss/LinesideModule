package com.jgm.lineside.interlocking;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This Class provides a Threaded Object that receives incoming messages from the Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class IncomingMessages extends DataInputStream implements Runnable {
    
    private Boolean connected = true;
    
    public IncomingMessages(InputStream in) {
        super(in);
    }

    @Override
    public void run() {
        while(true) {
            try {
                if (MessageHandler.isMessageFormattedCorrectly(this.readUTF())) {
                    
                } else {
                    
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }
    
    
    
    
}

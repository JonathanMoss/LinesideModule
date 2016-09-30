package com.jgm.lineside.interlocking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 *
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class OutgoingMessages extends DataOutputStream implements Runnable{

    public OutgoingMessages(OutputStream out) {
        super(out);
    }

    @Override
    public void run() {
        while (true) {
            
        }
    }
    
    public synchronized void sendMessageToRemoteInterlocking(String msg) {
        try {
            this.writeUTF(msg);
            this.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;



/**
 *
 * @author Jonathan Moss
 * @version 1.0 September 2016
 */
public class IncomingMessages extends DataInputStream implements Runnable{
    
    public IncomingMessages(InputStream in) {
        super(in);
    }

    @Override
    public void run() {
        while(true) {
            try {
                if (this.readUTF().equals("ALSAGER_REB|ACK|SETUP")) {
                    LineSideModule.remoteInterlocking.outgoing.sendPointsArrayToRemoteInterlocking(LineSideModule.getPointsArray());
                }
                System.out.println(this.readUTF());
            } catch (IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
    
    
}

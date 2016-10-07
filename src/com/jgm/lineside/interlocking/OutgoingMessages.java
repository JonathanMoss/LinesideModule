package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import com.jgm.lineside.points.Points;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

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
        System.out.println(msg);
        
        try {
            this.writeUTF(msg);
            this.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized void sendPointsArrayToRemoteInterlocking (ArrayList <Points> list) {
        
        int arrayLength = list.size();
        try {
            // The first Message should contain some additional details (always  fields)
            this.writeUTF(String.format("%s|%s|%s",
                    LineSideModule.getLineSideModuleIdentity(), "POINTS", arrayLength));
        } catch (IOException ex) {}
        
        for (int i = 0; i < arrayLength; i++) {
            try {
                this.writeUTF(String.format ("%s|%s",
                    LineSideModule.getLineSideModuleIdentity(), list.get(i).getIdentity()));
            } catch (IOException ex) {
                
            }
        }
        
    }

}

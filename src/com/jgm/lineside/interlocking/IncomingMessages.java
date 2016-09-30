package com.jgm.lineside.interlocking;

import java.io.DataInputStream;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}

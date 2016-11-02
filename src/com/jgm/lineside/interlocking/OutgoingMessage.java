package com.jgm.lineside.interlocking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This Class provides a Threaded Object that sends messages to the Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public final class OutgoingMessage extends DataOutputStream implements Runnable{

     private Boolean stayConnected = true; // The connection flag; whilst true, the Thread keeps running.
    
    /**
     * This is the Constructor Method for the OutgoingMessage Class Object.
     * @param out <code>OutputStream</code> object associated with the  connected Socket Object.
     */
    public OutgoingMessage(OutputStream out) {
        super(out);
        this.setMessageHandlerReference();
    }
    
    /**
     * This method informs the MessageHandler class that this object is being used to send outgoing messages.
     */
    public void setMessageHandlerReference() {
        MessageHandler.setOutgoing(this);
    }

    @Override
    public void run() {
        while (stayConnected) {} // This is to make sure this Thread stays live, when required.
    }
    
    /**
     * This method sends a message to the Remote Interlocking.
     * @param msg <code>String</code> that contains correctly formated message to be sent to the Remote Interlocking.
     */
    public synchronized void sendMessageToRemoteInterlocking(String msg) {
        
        try {
            this.writeUTF(msg);
            this.flush();
        } catch (IOException ex) {
            MessageHandler.closeConnections(); // There has been a problem; close the connections.
        }
    }
       
     /**
     * This method returns the stayConnected status flag.
     * @return <code>Boolean</code> <i>'true'</i> indicates that the thread should keep running, otherwise <i>'false'</i>.
     */
    protected Boolean getConnected() {
        return stayConnected;
    }

    /**
     * This method is used to set the stayConnected status flag.
     * @param connected <code>Boolean</code> <i>'true'</i> indicates that the thread should keep running, otherwise <i>'false'</i>.
     */
    protected void setConnected(Boolean connected) {
        this.stayConnected = connected;
    }

}

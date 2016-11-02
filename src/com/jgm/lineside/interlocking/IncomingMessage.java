package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import com.jgm.lineside.datalogger.Colour;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This Class provides a Threaded Object that receives incoming messages from the Remote Interlocking.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class IncomingMessage extends DataInputStream implements Runnable {
    
    private Boolean stayConnected = true; // The connection flag; whilst true, the Thread keeps running.
    
    /**
     * This is the Constructor Method for an IncomingMessages Class object.
     * @param in <code>InputStream</code> The InputStream object from the stayConnected Socket object.
     */
    public IncomingMessage(InputStream in) {
        super(in);
        this.setMessageHandlerReference();
    }

    /**
     * This method informs the MessageHandler class that this object is being used to receive incoming messages.
     */
    private void setMessageHandlerReference() {
        MessageHandler.setIncoming(this); // Register this object in the MessageHandler class.
    }
    /**
     * This method is automatically called when the IncomingMessage object receives a call to .start(); 
     */
    @Override
    public void run() {
        while(stayConnected) {
            try {
                String incomingMessage = this.readUTF(); // Read the incoming Message.
                if (MessageHandler.isMessageFormattedCorrectly(incomingMessage)) { // Check to see if the incoming message is of a valid format.
                    MessageHandler.addIncomingMessageToStack(incomingMessage); // Pass the message to the message handler; add it to the Incoming Message stack ready for processing.
                } // Otherwise, we ignore it!

            } catch (IOException ex) {
                LineSideModule.dataLogger.sendToDataLogger(String.format ("WARNING: The connection to the remote interlocking has been severed.",
                    Colour.RED.getColour(), Colour.RESET.getColour()), 
                    true, true);
                MessageHandler.closeConnections();
            }
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
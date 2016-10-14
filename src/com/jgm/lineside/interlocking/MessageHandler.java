package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import java.io.IOException;

/**
 * This Class provides static methods to handle and process incoming and outgoing messages.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class MessageHandler {
    
    private static final String MESSAGE_END = "MESSAGE_END"; 
    
    /**
     * This static method validates the format (not content) of a message.
     * @param message <code>String</code> containing the message to validate.
     * @return <code>Boolean</code> <i>'true'</i> is returned to indicate that the message format is correct, otherwise <i>'false'</i> is returned.
     */
    protected static synchronized Boolean isMessageFormattedCorrectly (String message) {
        /*
        *   Messages must be formatted thus: SENDER|TYPE|BODY|HASH|END_MESSAGE
        */
        
        String[] splitMessage = message.split("\\|");
        
        try {
        
            // Check the Message Length.
            if (splitMessage.length < 5) {
                throw new IOException("Message is incorrect length.");
            }
            
            // Check the Message Sender is valid.
            if (!splitMessage[0].matches("^[A-Z_0-9]{5,15}$")) {
                throw new IOException("The message sender is invalid.");
            }
            
            if (!splitMessage[0].equals(LineSideModule.getRiIdentity())) {
                throw new IOException("The message sender is invalid.");
            }
            
            // Check to see if a valid type has been specified.
            Boolean validType = false;
            for (MessageType value : MessageType.values()) {
                if (value.toString().equals(splitMessage[1])) {
                    validType = true;
                    break;
                } 
            }
            MessageType type = MessageType.valueOf(splitMessage[1]);
            
            if (!validType) {
                throw new IOException ("Invalid message type.");
            }
            
            // Check to make sure that the message body contains valid text.
            if (!splitMessage[2].matches("^[A-Z_0-9.-]{4,}$")) {
                throw new IOException ("Invalid message body");
            }
            
            
            // Check HashCode.
            if (String.format ("%s|%s|%s", splitMessage[0], type.toString(), splitMessage[2]).hashCode()!= Integer.parseInt(splitMessage[3])) {
                throw new IOException("Message hash code is invalid");
            }
            
            // Check Message End.
            if (!splitMessage[4].equals(MESSAGE_END)) {
                throw new IOException ("Incorrectly formatted message end.");
            }
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        } 
        
        return true;
    }
    

}

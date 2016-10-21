package com.jgm.lineside.interlocking;

/**
 * The Message Class provides objects representing Message that have been received or that require transmitting.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016.
 */

public class Message {

    private final MessageType msgType; // The type of the message, i.e. STATE_CHANGE, HAND_SHAKE etc...
    private final String msgBody; // The message body.
    private final int msgHash; // The hash of the relevant portion of the message.
    
    /**
    * This is the Constructor method for the Message Class object.
    *  
    * @param type A <code>MessageType</code> constant that informs the receiving module regarding the purpose of the message.
    * @param message A <code>String</code> that contains that actual message body text.
    * @param hash An <code>int</code> that contains the hashCode of the relevant portions of the message.
    */
    protected Message (MessageType type, String message, int hash) {
        
        // Assign the values received in the constructor to the instance variables.
        this.msgBody = message;
        this.msgHash = hash;
        this.msgType = type;
    
    }

    /**
     * This method returns the type of message.
     * @return <code>MessageType</Code> Indicates to the receiver the reason the message was sent.
     */
    protected MessageType getMsgType() {
        return msgType;
    }

    /**
     * This method returns the body of the message, i.e. the message content.
     * @return <code>String</Code> containing the message body.
     */
    protected String getMsgBody() {
        return msgBody;
    }

    /**
     * This method returns the hashCode of the relevant portions of the message.
     * @return <code>Integer</Code> containing the hash code of the message.
     */
    protected int getMsgHash() {
        return msgHash;
    }

}

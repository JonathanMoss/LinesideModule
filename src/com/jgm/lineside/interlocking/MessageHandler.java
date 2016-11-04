package com.jgm.lineside.interlocking;

import com.jgm.lineside.LineSideModule;
import com.jgm.lineside.points.PointsPosition;
import com.jgm.lineside.signals.MovementAuthorityClass;
import com.jgm.lineside.signals.SignalAspect;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

/**
 * This Class provides static methods to handle and process incoming and outgoing messages.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class MessageHandler {
    
    private static final String MESSAGE_END = "MESSAGE_END"; // Constant that MUST be the last portion of all messages!
    private static final LinkedList <Message> OUTGOING_STACK = new LinkedList<>(); // An ArrayList that contains all outgoing messages.
    private static final LinkedList <Message> INCOMING_STACK = new LinkedList<>(); // An ArrayList that contains all incoming messages.
    private static OutgoingMessage outgoing = null; // The OutgoingMessage object used to send messages to the Remote Interlocking.
    private static IncomingMessage incoming = null; // The IncomingMessage object where messages received from the Remote Interlocking are received.
    private static Socket connectionToRemoteInterlocking = null; // The Socket (Connection) to the Remote Interlocking.
    
    /**
     * This method closes the connection to the Remote Interlocking.
     */
    public static synchronized void closeConnections() {
        
        try {
            outgoing.setConnected(false);
            outgoing.close();
            incoming.setConnected(false);
            incoming.close();
            connectionToRemoteInterlocking.close();
        } catch (IOException ex) {}
        
    } 
    
    /** 
     * This method removes messages from the outgoing message stack, based on hash.
     * @param hash <code>Integer</code> The hash code of the message that should be removed from the outgoing message stack.
     */
    private static synchronized void removeAcknowledgedMessage (int hash) {
        
        for (int i = 0; i < OUTGOING_STACK.size(); i++) { // Loop through the stack.
            if (OUTGOING_STACK.get(i).getMsgHash() == hash) {
                OUTGOING_STACK.remove(i); // Remove the message as the hash's match.
                break;
            }
        }
    }
    
    /**
     * This method processes each message contained within the incoming message stack.
     */
    public static synchronized void processIncomingMessages() {
    
        while (!INCOMING_STACK.isEmpty()) { // Proceed if there are messages in the Incoming Message Stack (Queue)
            switch (INCOMING_STACK.get(0).getMsgType()) { // Get the type of message.
                
                case ACK:
                    /*
                     * Remove the corresponding message from the OUTGOING_MESSAGE_STACK.
                     * Remember, the message body of an ACK message contains the hash code
                     * sent message that requires acknowledgement.
                     */
                    removeAcknowledgedMessage(Integer.parseInt(INCOMING_STACK.get(0).getMsgBody()));
                    break;

                case STATE_CHANGE:
                    break;
                
                case REQUEST:
                   /*
                    *   Examples:
                    *   POINTS.994.REVERSE
                    *   SIGNAL.CE.110.CE.112.MAIN.NULL
                    *   SIGNAL.CE.110.CE.112.MAIN.YELLOW
                    */
                    String[] splitMessage = INCOMING_STACK.get(0).getMsgBody().split("\\.");
                    switch (splitMessage[0]) {
                        case "POINTS":
                            LineSideModule.incomingPointsRequest(   splitMessage[1], 
                                                                    PointsPosition.valueOf(splitMessage[2]));
                            break;
                        case "CONTROLLED_SIGNAL":
                            LineSideModule.incomingControlledSignalRequest( splitMessage[1], 
                                                                            splitMessage[2], 
                                                                            splitMessage[3], 
                                                                            splitMessage[4], 
                                                                            MovementAuthorityClass.valueOf(splitMessage[5]),
                                                                            splitMessage[6]);
                            break;
                        case "AUTOMATIC_SIGNAL":
                            LineSideModule.incomingAutomaticSignalRequest(  splitMessage[1], 
                                                                            splitMessage[2], 
                                                                            Boolean.valueOf(splitMessage[3]));
                            break;
                                    
                    }
                    
                    break;
                       
            }
            
            INCOMING_STACK.remove(0); // Remove the message we have just processed from the message stack.
        }
        
    }
    
    /**
     * This method processes each message contained within the outgoing message stack.
     */
    public static synchronized void processOutgoingMessages() {
    
        while (!OUTGOING_STACK.isEmpty()) {
            
            // Send Message
            sendMessage (OUTGOING_STACK.get(0));
            // Remove from OUTGOING_STACK
            OUTGOING_STACK.remove(0);
                 
        }
    }
    
    /**
     * This method formats and sends a message to the OutgoingMessage object.
     * 
     * This method receives a Message Object, it then formats a String object and
     * passes the formatted message to the OutgoingMessage object to send to the Remote Interlocking.
     * This method should only be called from within the processOutgoingMessages method.
     * 
     * @param message A <code>Message</code> object required to be sent to the Remote Interlocking.
     */
    private static synchronized void sendMessage(Message message) {
       
        /*
        *   Messages must be formatted thus: SENDER|TYPE|BODY|HASH|END_MESSAGE
        */
      
        outgoing.sendMessageToRemoteInterlocking(String.format ("%s|%s|%s|%s|%s", 
            LineSideModule.getLineSideModuleIdentity(), 
            message.getMsgType().toString(), 
            message.getMsgBody(), 
            message.getMsgHash(), 
            MESSAGE_END));
        
    }
    
    /**
     * This method adds a message to the incoming message stack ready for processing.
     * 
     * Note: This method does not perform any checks on the message, this should be don prior to passing the message to this method.
     * @param message A <code>String</code> that contains the message, as received from the Remote Interlocking, i.e. incomplete.
     */
    protected static synchronized void addIncomingMessageToStack(String message) {
        
        // Take the incoming message, and split it into 5 parts.
        String[] splitMessage = message.split("\\|");
        
        // Create a new Message object and assign to the Incoming stack ArrayList.
        INCOMING_STACK.add(new Message(
            MessageType.valueOf(splitMessage[1]), splitMessage[2], Integer.parseInt(splitMessage[3])));
        
    }
    
    /**
     * This method adds a message to the outgoing message stack ready for processing.
     * 
     * @param type A <code>MessageType</code> constant, indicating the type of message.
     * @param message A <code>String</code> that contains the message, as received from the Remote Interlocking, i.e. incomplete.
     */
    public static synchronized void addOutgoingMessageToStack(MessageType type, String message) {
        String sender = LineSideModule.getLineSideModuleIdentity();
        int hashCode = String.format ("%s|%s|%s", sender, type.toString(), message).hashCode();
        OUTGOING_STACK.add(new Message(type, message, hashCode));
    }
    
    /**
     * This static method validates the format (not content) of a message.
     * @param message <code>String</code> containing the message to validate.
     * @return <code>Boolean</code> <i>'true'</i> is returned to indicate that the message format is correct, otherwise <i>'false'</i> is returned.
     */
    protected static synchronized Boolean isMessageFormattedCorrectly (String message) {
        /*
        *   Messages must be formatted thus: SENDER|TYPE|BODY|HASH|END_MESSAGE
        */
        
        System.out.println(message);
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
            if (!splitMessage[2].matches("^[A-Za-z_0-9.-_]{4,}$")) {
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

    /**
     * This method returns the OutgoingMessage object.
     * @return The <code>OutgoingMessage</code> object associated with the Socket Connection to the Remote Interlocking. 
     */
    public static OutgoingMessage getOutgoing() {
        return outgoing;
    }
    
    /**
     * This method sets the OutgoingMessage object.
     * @param aOutgoing The <code>OutgoingMessage</code> object associated with the Socket Connection to the Remote Interlocking. 
     */
    public static void setOutgoing(OutgoingMessage aOutgoing) {
        outgoing = aOutgoing;
    }
    
    /**
     * This method returns the IncomingMessage object.
     * @return <code>IncomingMessage</code> object, associated with the Socket Connection to the RemoteInterlocking.
     */
    public static IncomingMessage getIncoming() {
        return incoming;
    }

    /**
     * This method sets the IncomingMessage object.
     * @param aIncoming <code>IncomingMessage</code> object, associated with the Socket Connection to the RemoteInterlocking.
     */
    public static void setIncoming(IncomingMessage aIncoming) {
        incoming = aIncoming;
    }

    /**
     * This method returns the Socket Object that is connected to the RemoteInterlocking.
     * @return <code>Socket</code> The connection to the RemoteInterlocking.
     */
    public static Socket getConnectionToRemoteInterlocking() {
        return connectionToRemoteInterlocking;
    }

    /**
     * This method sets the Socket Connection to the Remote Interlocking.
     * @param aConnectionToRemoteInterlocking <code>Socket</code> The connection to the RemoteInterlocking.
     */
    public static void setConnectionToRemoteInterlocking(Socket aConnectionToRemoteInterlocking) {
        connectionToRemoteInterlocking = aConnectionToRemoteInterlocking;
    }
    
}

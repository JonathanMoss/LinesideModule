package com.jgm.lineside;

import java.util.Arrays;

/**
 * This class provides the blueprint for a physical representation of a controlled signal.
 * There is no interlocking logic provided by this class.
 * 
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class Controlled_Signal {
    
    private final Controlled_Signal_Type signalType;
    private Aspects currentAspect;
    private final String prefix;
    private final String id;
    private final LampStatus[] signalLamp;
    
    /**
     * This is the constructor method for the Signal Class.
     * @param prefix A <code>String</code> representing the signal prefix.
     * @param id A <code>String</code> representing the signal identity.
     * @param signalType A <code>Controlled_Signal_Type</code> object representing the type of Signal being instantiated.
     * 
     * @throws Exception
     */
    protected Controlled_Signal (String prefix, String id, Controlled_Signal_Type signalType) throws Exception{
        
        // Parameter Validation.   
        if (!prefix.toUpperCase().matches("^[A-Z]{2,3}[A-Za-z0-9]{0,1}$")) {
            throw new Exception("Invalid Signal Prefix passed to constructor.");
        } else {
            this.prefix = prefix.toUpperCase();
        }
        
        if (!id.toUpperCase().matches("^[0-9]{2,4}[R]{0,1}$")) {
            throw new Exception("Invalid Signal ID passed to constructor.");
        } else {
            this.id = id.toUpperCase();
        }

        this.signalType = signalType;
        
        // Setup the initial aspect.
        this.currentAspect = Aspects.RED;
       
        
        // Setting up a signal lamp for each aspect, except the aspects that cannot be shown - such as black!.
        int x;
        if (this.signalType.equals(Controlled_Signal_Type.COLOUR_LIGHT_4)) {
            x = 2;
        } else {
            x = 1;
        }
        this.signalLamp = new LampStatus[(this.signalType.returnApplicableAspects().length - x)];
        
        for (int i = 0; i < (this.signalType.returnApplicableAspects().length - x); i++) {
            // Set the intial status of each lamp to OK.
            this.signalLamp [i] = LampStatus.OK;
            //System.out.println(String.format("%s aspect lamp: %s", this.signalType.returnApplicableAspects()[i], this.signalLamp[i].toString()));
        }
                
    }
    
    /**
     * This method causes a signal lamp to shown as either 'blown' or 'ok' - simulating a traditional filament failure or its restoration.
     * 
     * @param signal An <code>Aspects</code> object representing the signal aspect that the operation should be undertaken on.
     * @param status A <code>LampStatus</code> object representing the intended status of the lamp.
     */
    protected void filamentFailure (Aspects signal, LampStatus status) {
        for (int i = 0; i < this.signalLamp.length; i++) {
            if (this.signalType.returnApplicableAspects()[i].equals(signal) && this.signalLamp[i] != status) {
                this.signalLamp[i] = status;
                this.requestSignalAspect(this.currentAspect);
                break;
            }
        }
    }
    
    /**
     * This method causes a signal lamp to shown as either 'blown' or 'ok' - simulating a traditional filament failure or its restoration.
     * 
     * @param lamp An <code>integer</code> representing the signal aspect that the operation should be undertaken on.
     * @param status A <code>LampStatus</code> object representing the intended status of the lamp.
     */
    protected void filamentFailure (int lamp, LampStatus status) {
        if (lamp < this.signalLamp.length && this.signalLamp[lamp] != status) {
            this.signalLamp[lamp] = status;
            this.requestSignalAspect(this.currentAspect);
        }
    }
    
    /**
     * This method returns the current signal aspect;
     * 
     * @return An <code>Aspects</code> object representing the current signal aspect.
     */
    protected Aspects getCurrentAspect() {
        return this.currentAspect;
    }
    
    /**
     * This method is used to attempt to set the aspect of the signal.
     * 
     * @param requestedAspect An <code>integer</code> value representing the aspect that the remote interlocking requires the signal to display.
     * @return An <code>Aspects</code> object indicating the current Controlled_Signal Aspect after the method has ran.
     */
    protected Aspects requestSignalAspect (Aspects requestedAspect) {
        
        // BLACK and TOP_YELLOW aspects are not valid requested aspects. They are an effect of other conditions - check to make sure they have not been requested.
        if (requestedAspect.equals(Aspects.BLACK) || requestedAspect.equals(Aspects.TOP_YELLOW)) {
            return this.currentAspect; // Do nothing and return the current aspect without any changes.
        }
        
        // 1) Check that the requested aspect is a valid request for the signal type.
        if (Arrays.toString(this.signalType.returnApplicableAspects()).contains(requestedAspect.toString())) {
            int aspectIndex = 0;
            // Get the position of the aspect in the SignalType array by looping through.
            for (int i = 0; i < this.signalType.returnApplicableAspects().length - 1; i++) {
                if (this.signalType.returnApplicableAspects()[i].equals(requestedAspect)) {
                    aspectIndex = i;
                    break;
                }
            }
        
            // The 4 aspect color light signal causes us a few headaches and is perculiar. The following code block
            // addresses these issues specifically. Might change this at a later date though - looks a bit messy!
            if (requestedAspect == Aspects.DOUBLE_YELLOW && this.signalLamp[aspectIndex] == LampStatus.BLOWN) {
                this.currentAspect = Aspects.SINGLE_YELLOW;
            } else if (requestedAspect == Aspects.DOUBLE_YELLOW && this.signalLamp[(aspectIndex - 1)] == LampStatus.BLOWN) {
                this.currentAspect = Aspects.TOP_YELLOW;
            } else {
                
                // The main code block showing either the correct aspect or black, becasue the lamp has blown.
                if (this.signalLamp[aspectIndex] == LampStatus.BLOWN) {
                    this.currentAspect = Aspects.BLACK;
                } else {
                    this.currentAspect = requestedAspect;
                }
        
            }
            
        }
        return this.currentAspect;

    }
    
    /**
     * This method returns the type of the signal.
     * 
     * @return A <code>Controlled_Signal_Type</code> object that represents the type of signal.
     */
    protected Controlled_Signal_Type getSignalType() {
        return this.signalType;
    }
    
    /**
     * This method returns the full signal identity - prefix and identity as a single string.
     * 
     * @return A <code>String</code> containing the signal prefix and signal identity.
     */
    protected String getFullSignalIdentity() {
        return (this.prefix + this.id);
    }
    
}
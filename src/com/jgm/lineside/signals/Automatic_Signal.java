package com.jgm.lineside.signals;

import com.jgm.lineside.signals.Aspects;
import java.util.HashMap;

/**
 *
 * @author Jonathan Moss
 */
public class Automatic_Signal {
    
    private static int automaticSignalTally = 0; // A static class variable to contain an integer regarding how many Automatic Signals have been created.
    private final static HashMap <String, Integer> AUTOMATIC_SIGNALS_HM = new HashMap <>(); // Map to store each Train Detection Index and Signal Identity.
    private final String prefix;
    private final String identity;
    private final Automatic_Signal_Type type;
    private final String line;
    private final String readDirection;
    private final Function function;
    private final String applicableSignal;
    private Aspects currentAspect;
    
    /**
     * This is the constructor for an Automatic_Signal object.
     * 
     * An automatic signal is defined as any signal that is not controlled by the actions of the Signaller.
     * @param prefix
     * @param identity
     * @param type
     * @param line
     * @param readDirection
     * @param function
     * @param applicableSignal 
     */
    public Automatic_Signal (String prefix, String identity, Automatic_Signal_Type type, String line, String readDirection, Function function, String applicableSignal) {
        this.prefix = prefix;
        this.identity = identity;
        this.type = type;
        this.line = line;
        this.readDirection = readDirection;
        this.function = function;
        this.applicableSignal = applicableSignal;
        Automatic_Signal.AUTOMATIC_SIGNALS_HM.put(this.identity, Automatic_Signal.automaticSignalTally);
        Automatic_Signal.automaticSignalTally ++;
        this.currentAspect = this.type.returnApplicableAspects()[0];
    }
    
    public String getSignalIdentity() {
        return (this.prefix + this.identity);
    }
    
    public Automatic_Signal_Type getType() {
        return this.type;
    }
    
    public Function getFunction() {
        return this.function;
    }
    
    public Aspects getCurrentAspect() {
        return this.currentAspect;
    }
    
    /**
    * Where Automatic Signal objects are instantiated within an array, this method returns the associated index integer (Assuming this is the case).
    * Further, to be effective, all Automatic Signal objects should be instantiated within a single array.
    * 
    * This method is required by the Technicians Interface and Interlocking Components.
    * 
    * @param identity a <code>String</code> indicating the identity of the points.
    * @return <code>integer</code> representing the array index of the Signal object within the array.
    */
    public static int returnAutomaticSignalIndex(String identity) {
        return Automatic_Signal.AUTOMATIC_SIGNALS_HM.get(identity);  
    }
    
}





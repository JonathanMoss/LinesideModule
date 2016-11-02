package com.jgm.lineside.signals;

/**
 * This Class provides the blueprint for a Controlled Signal.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ControlledSignal extends Signal {

    String toSignalPrefix, toSignalIdentity;
    
    /**
     * This is the Constructor Method for a ControlledSignal Object
     * 
     * @param signalPrefix <code>String</code> The Prefix of the signal.
     * @param signalIdentity <code>String</code> The Identity of the signal.
     * @param signalType <code>SignalType</code> The Type of Signal.
     */
    public ControlledSignal(String signalPrefix, String signalIdentity, SignalType signalType) {
        
        super(signalPrefix, signalIdentity, signalType);
        super.setDisplayHighestAspect(false);
        
    }
    
    /**
     * This method attempts to clear the signal to its highest aspect.
     * @param toSignalPrefix <code>String</code> The Prefix of the signal that the route is being set to.
     * @param toSignalIdentity <code>String</code> The Identity of the signal that the route is being set to.
     */
    public void setSignal (String toSignalPrefix, String toSignalIdentity) {
        
        this.toSignalPrefix = toSignalPrefix;
        this.toSignalIdentity = toSignalIdentity;
        
        super.informApplicableSignal(toSignalPrefix, toSignalIdentity);
        super.setDisplayHighestAspect(true);
        
    }
    
    /**
     * This method attempts to clear the signal, to the highest aspect passed with the parameters.
     * 
     * This method is used when, for example setting an approach control from other than red.
     * 
     * @param toSignalPrefix <code>String</code> The Prefix of the signal that the route is being set to.
     * @param toSignalIdentity <code>String</code> The Identity of the signal that the route is being set to.
     * @param highestAspect <code>SignalAspect</code> The highest aspect that the signal should display.
     */
    public void setSignal (String toSignalPrefix, String toSignalIdentity, SignalAspect highestAspect) {
    
        this.toSignalPrefix = toSignalPrefix;
        this.toSignalIdentity = toSignalIdentity;
        
        super.informApplicableSignal(toSignalPrefix, toSignalIdentity);
        super.setDisplayHighestAspect(highestAspect);
        
    }
    
    /**
     * This method returns the controlled signal to danger.
     */
    public void signalOn() {
        
        super.setDisplayHighestAspect(false);
        Signal.getSignalObject(this.toSignalPrefix, this.toSignalIdentity).removeSignalInRear(this);
        
    }
    
}

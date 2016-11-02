package com.jgm.lineside.signals;

/**
 * This Enumeration provides constant values for the classification of movement authority.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public enum MovementAuthorityClass {

    /**
     * Not a class of Movement Authority but used to signify to the Signal that it should be set to ON.
     */
    SIGNAL_ON,
    
    /**
     * The route and designated overlap are clear.
     */
    MAIN,
    
    /**
     * The route is clear but only a restricted overlap is provided.
     */
    WARNING,
    
    /**
     * The route is occupied.
     */
    CALLING_ON,
    
    /**
     * The route is required for shunting purposes; it is permitted to be clear or occupied.
     */
    SHUNT,
    
    /**
     * The route is provided for use during degraded situations.
     */
    POSA;
    
}

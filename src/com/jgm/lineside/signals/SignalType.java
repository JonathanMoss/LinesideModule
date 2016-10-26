package com.jgm.lineside.signals;

/**
 * This Enum provides a model of signal types and applicable signal aspects for each type.
 * 
 * @author Jonathan Moss
 * @version v2.0 October 2016
 */
public enum SignalType {
    
    /**
     * Buffer Stops, LOS and Fixed Red Colour Light Signals.
     */
    FIXED_RED(SignalAspect.RED, SignalAspect.BLACK),
    
    /**
     * Ground Position Light Signals.
     */
    POS_LIGHT(SignalAspect.RED, SignalAspect.SUB_OFF, SignalAspect.BLACK),
    
    /**
     * Proceed On Sight Aspect Signals.
     */
    POS_LIGHT_POSA(SignalAspect.RED, SignalAspect.SUB_OFF, SignalAspect.FLASHING_WHITE, SignalAspect.BLACK),
    
    /**
     * Banner Repeater Signals.
     */
    BANNER(SignalAspect.CAUTION, SignalAspect.CLEAR, SignalAspect.BLACK), 
    
    /**
     * Colour Light Repeater Signals.
     */
    COLOUR_LIGHT_REPEATER(SignalAspect.CAUTION, SignalAspect.CLEAR, SignalAspect.BLACK),
    
    /**
     * Colour Light Signal with 3 Aspects.
     */
    COLOUR_LIGHT_3(SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_YELLOW, SignalAspect.BLACK),
    
    /**
     * Co-Acting Colour Light Signal with 3 Aspects.
     */
    CA_COLOUR_LIGHT_3(SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_YELLOW, SignalAspect.BLACK),
    
    /**
     * 3 Aspect Colour Light Signals with a Co-Acting position Light.
     */
    COLOUR_LIGHT_3_CA(SignalAspect.SUB_OFF, SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.GREEN, SignalAspect.BLACK),
    
    /**
     * 3 Aspect Colour Light Signals with a POSA.
     */
    COLOUR_LIGHT_3_CA_POSA(SignalAspect.SUB_OFF, SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_WHITE, SignalAspect.BLACK),
    
    /**
     * 4 Aspect Colour Light Signal.
     */
    COLOUR_LIGHT_4(SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.DOUBLE_YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_YELLOW,  SignalAspect.FLASHING_DOUBLE_YELLOW, SignalAspect.TOP_YELLOW, SignalAspect.BLACK),
    
    /**
     * Co-Acting Colour Light Signal with 3 Aspects.
     */
    CA_COLOUR_LIGHT_4(SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.DOUBLE_YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_YELLOW,  SignalAspect.FLASHING_DOUBLE_YELLOW, SignalAspect.TOP_YELLOW, SignalAspect.BLACK),
    
    /**
     * 4 Aspect Colour Light Signal with a Co-Acting position Light.
     */
    COLOUR_LIGHT_4_CA(SignalAspect.SUB_OFF, SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.DOUBLE_YELLOW, SignalAspect.GREEN,  SignalAspect.FLASHING_YELLOW, SignalAspect.FLASHING_DOUBLE_YELLOW,  SignalAspect.TOP_YELLOW, SignalAspect.BLACK),
    
    /**
     * 4 Aspect Colour Light Signal with a POSA.
     */
    COLOUR_LIGHT_4_CA_POSA(SignalAspect.SUB_OFF, SignalAspect.RED, SignalAspect.YELLOW, SignalAspect.DOUBLE_YELLOW, SignalAspect.GREEN, SignalAspect.FLASHING_YELLOW, SignalAspect.FLASHING_DOUBLE_YELLOW, SignalAspect.TOP_YELLOW, SignalAspect.FLASHING_WHITE, SignalAspect.BLACK),
    
    /**
     * SPAD (Signal Passed at Danger) Indicator.
     */
    SPAD_INDICATOR(SignalAspect.SPAD_INDICATOR_ILLUMINATED, SignalAspect.BLACK);
    
    private final SignalAspect[] applicable_aspects;
    
    SignalType(SignalAspect... applicable_aspects) {
        
        this.applicable_aspects = applicable_aspects;
    }
    
    SignalAspect[] returnApplicableSignalAspect() {
       
        return this.applicable_aspects;
        
    }

}

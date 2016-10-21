package com.jgm.lineside.signals;

/**
 *
 * @author Jonathan Moss
 */
public enum SignalType {
    
    POS_LIGHT(Aspects.RED, Aspects.SUB_OFF, Aspects.BLACK), 
    COLOUR_LIGHT_3(Aspects.RED, Aspects.YELLOW, Aspects.GREEN, Aspects.BLACK), 
    COLOUR_LIGHT_4(Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.DOUBLE_YELLOW, Aspects.GREEN, Aspects.TOP_YELLOW, Aspects.BLACK),
    COLOUR_LIGHT_3_CA(Aspects.SUB_OFF, Aspects.RED, Aspects.YELLOW, Aspects.GREEN, Aspects.BLACK), 
    COLOUR_LIGHT_4_CA(Aspects.SUB_OFF, Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.DOUBLE_YELLOW, Aspects.GREEN, Aspects.TOP_YELLOW, Aspects.BLACK);
    
    private final Aspects[] applicable_aspects;
    
    SignalType(Aspects... applicable_aspects) {
        
        this.applicable_aspects = applicable_aspects;
    }
    
    Aspects[] returnApplicableAspects() {
       
        return this.applicable_aspects;
        
    }

}

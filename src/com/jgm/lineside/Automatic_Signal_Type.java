package com.jgm.lineside;

/**
 *
 * @author Jonathan Moss
 */
public enum Automatic_Signal_Type {
    
    FIXED_RED(Aspects.RED, Aspects.BLACK), 
    COLOUR_LIGHT_2(Aspects.YELLOW, Aspects.GREEN, Aspects.BLACK), 
    COLOUR_LIGHT_3(Aspects.RED, Aspects.YELLOW, Aspects.GREEN, Aspects.BLACK), 
    COLOUR_LIGHT_4(Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.DOUBLE_YELLOW, Aspects.GREEN, Aspects.TOP_YELLOW, Aspects.BLACK),
    BANNER_REPEATER(Aspects.CAUTION, Aspects.CLEAR, Aspects.BLACK);
    
    private final Aspects[] applicable_aspects;
    
    Automatic_Signal_Type(Aspects... applicable_aspects) {
        this.applicable_aspects = applicable_aspects;
    }
    
    Aspects[] returnApplicableAspects() {
        return this.applicable_aspects;
    }
    
}

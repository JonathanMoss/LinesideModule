package com.jgm.lineside.signals;

import com.jgm.lineside.signals.Aspects;

/**
 *
 * @author Jonathan Moss
 */
public enum Controlled_Signal_Type {
    
    POS_LIGHT(Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.BLACK), 
    COLOUR_LIGHT_3(Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.GREEN, Aspects.BLACK), 
    COLOUR_LIGHT_4(Aspects.RED, Aspects.SINGLE_YELLOW, Aspects.DOUBLE_YELLOW, Aspects.GREEN, Aspects.TOP_YELLOW, Aspects.BLACK);
    
    private final Aspects[] applicable_aspects;
    
    Controlled_Signal_Type(Aspects... applicable_aspects) {
        
        this.applicable_aspects = applicable_aspects;
    }
    
    Aspects[] returnApplicableAspects() {
       
        return this.applicable_aspects;
        
    }

}

package com.jgm.lineside.signals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This Class provides the Superclass functionality for a Signal Object.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class Signal implements Signals {
    
    public static ArrayList <Signal> SIGNAL_ARRAY; // The Arraylist that contains a reference to all the Signal Objects.
    
    private final String prefix; // The Signal prefix.
    private final String identity; // The Signal Identity.
    private final SignalType signalType; // The type of Signal.
    private final ArrayList <Signal> signalsInRear; // An ArrayList that contains all signals that require an Aspect Update.
    private SignalAspect applicableSignalAspect; // The aspect of the signal ahead.
    private SignalAspect currentAspect; // The current aspect of the signal.
    private final Map <SignalAspect, Boolean> signalLamps = new LinkedHashMap<>(); // A Map that contains the Signal Lamps and an indication if the lamps are proved to be lit.
    private Boolean displayHighestAspect = false; // A Flag to set if the signal can display the highest aspect available - in other words step up.
    private SignalAspect doNotStepHigherThan = null;
    
    /**
     * This method allows this class to reference the ArrayList that contains all Signal Objects.
     * @param signalArray <code>ArrayList</code> The ArrayList that contains all Signal Objects.
     */
    public static void setSignalArray (ArrayList signalArray) {
        SIGNAL_ARRAY = signalArray;
    }
    
    /**
     * This method returns a Signal Object.
     * 
     * The Signal Prefix and Identity of the Signal Object are passed into this method.
     * The method returns the relevant Signal Object.
     * 
     * @param prefix <code>String</code> The Prefix of the signal.
     * @param identity <code>String</code> The Identity of the signal.
     * @return <code>Signak</code> The Signal Object.
     */
    public static Signal getSignalObject (String prefix, String identity) {
    
        for (int i = 0; i < SIGNAL_ARRAY.size(); i++) {
            if (SIGNAL_ARRAY.get(i).getPrefix().equals(prefix) && SIGNAL_ARRAY.get(i).identity.equals(identity)) {
                return SIGNAL_ARRAY.get(i);
            }
        }
        return null;
    }
    
    /**
     * This is the Constructor Method for a Signal Object.
     * 
     * @param signalPrefix <code>String</code> The Prefix of the signal.
     * @param signalIdentity <code>String</code> The Identity of the signal.
     * @param signalType <code>SignalType</code> The Type of Signal.
     */
    public Signal (String signalPrefix, String signalIdentity, SignalType signalType) {
        
        this.signalsInRear = new ArrayList<>(); // Setup up the Signals In Rear ArrayList.
        this.prefix = signalPrefix; // Assign the Signal Prefix.
        this.identity = signalIdentity; // Assign the Signal Identity.
        this.signalType = signalType; // Assign the Signal Type.
        this.createSignalLamps(); // Call the method that creates Signal Lamps for each of the relevant Aspects.
        this.DisplayHighestAspect(); // Call the method to assign an aspect into the Signal based on circumstances.
        
    }

    @Override
    public void addSignalInRear (Signal signalInRear) {
        
        // Check to make sure that there is not an identical entry within the ArrayList prior to inserting.
        if (!this.signalsInRear.contains(signalInRear)) {
            this.signalsInRear.add(signalInRear);
        }
        
        this.updateSignalsInRear(); // Call the method to update all signals in rear with the current Signal Aspect.
        
    }

    @Override
    public void removeSignalInRear (Signal signalInRear) {
        if (!this.signalsInRear.contains(signalInRear)) {
            this.signalsInRear.remove(signalInRear);
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public SignalType getSignalType() {
        return this.signalType;
    }

    @Override
    public void applicableSignalAspectUpdate(SignalAspect aspect) {
        this.applicableSignalAspect = aspect;
        this.DisplayHighestAspect(); 
        
    }
    
    @Override
    public void informApplicableSignal (String applicableSignalPrefix, String applicableSignalIdentity) {
        
        Signal signalObject = Signal.getSignalObject(applicableSignalPrefix, applicableSignalIdentity);
        
        if (signalObject != null) {
            
            signalObject.addSignalInRear(this);
            
        }
        
    }

    @Override
    public void updateSignalsInRear() {
    
        SignalAspect curAspect = this.currentAspect;
        
        if (this.currentAspect == SignalAspect.TOP_YELLOW) {
            curAspect = SignalAspect.DOUBLE_YELLOW;
        }
        
        for (int i = 0; i < this.signalsInRear.size(); i++) {
            this.signalsInRear.get(i).applicableSignalAspectUpdate(curAspect);
        }
        
    }

    @Override
    public SignalAspect getCurrentAspect() {
        return this.currentAspect;
    }

    @Override
    public SignalAspect calculateBestAspect() {
        
        if (this.applicableSignalAspect == null) {
            this.applicableSignalAspect = SignalAspect.RED; // Assign a default where no Aspect for the Applicable Signal has been provided (yet).
        }
        
        // If the Signal is Co-Acting, then the Signal should display an identical aspect.
        if (this.getIdentity().contains("CA")) {
            return this.applicableSignalAspect;
        }
        
        // Look at what the Applicable Signal is currently displaying, then decide on an apppropriate aspect to display in this signal.
        switch (this.applicableSignalAspect) { 
        
            case SPAD_INDICATOR_ILLUMINATED:
            case BLACK:
            case TOP_YELLOW:
                if (this instanceof RepeaterSignal) {
                    return SignalAspect.CAUTION;
                } else {
                    return SignalAspect.RED;
                }
                
            case SUB_OFF:
            case FLASHING_WHITE:
            case RED:
                if (this instanceof RepeaterSignal) {
                    return SignalAspect.CAUTION;
                } else {
                    return SignalAspect.YELLOW;
                }
                
            case YELLOW:
                if (this instanceof RepeaterSignal) {
                    return SignalAspect.CLEAR;
                } else {
                    if (this.getSignalType().toString().contains("_4")) {
                        return SignalAspect.DOUBLE_YELLOW;
                    } else {
                        return SignalAspect.GREEN;
                    }
                }
                
            case GREEN:
            case FLASHING_DOUBLE_YELLOW:
            case DOUBLE_YELLOW:
                if (this instanceof RepeaterSignal) {
                    return SignalAspect.CLEAR;
                } else {
                    return SignalAspect.GREEN;
                }
            
            case FLASHING_YELLOW:
                if (this instanceof RepeaterSignal) {
                    return SignalAspect.CLEAR;
                } else {
                    if (this.getSignalType().toString().contains("_4")) {
                        return SignalAspect.FLASHING_DOUBLE_YELLOW;
                    } else {
                        return SignalAspect.GREEN;
                    }
                }

        }
        
        return SignalAspect.RED;
        
    }

    @Override
    public final void createSignalLamps() {
        
        int numberOfApplicableAspects = 0;
        int startingPointWithinArray = 0;
        
        switch (this.getSignalType()) {
            
            case SPAD_INDICATOR:
            case FIXED_RED:
                numberOfApplicableAspects = 1;
                startingPointWithinArray = 0;
                break;
                
            case COLOUR_LIGHT_REPEATER:
            case BANNER:
            case POS_LIGHT:
                numberOfApplicableAspects = 2;
                startingPointWithinArray = 0;
                break;
                
            case CA_COLOUR_LIGHT_3:
            case COLOUR_LIGHT_3:
            case POS_LIGHT_POSA:
                numberOfApplicableAspects = 3;
                startingPointWithinArray = 0;
                break;
                
            case COLOUR_LIGHT_3_CA_POSA:
            case COLOUR_LIGHT_3_CA:
                numberOfApplicableAspects = 3;
                startingPointWithinArray = 1;
                break;
            
            case CA_COLOUR_LIGHT_4:
            case COLOUR_LIGHT_4:
                numberOfApplicableAspects = 4;
                startingPointWithinArray = 0;
                break;
                
            case COLOUR_LIGHT_4_CA:
            case COLOUR_LIGHT_4_CA_POSA:
                numberOfApplicableAspects = 4;
                startingPointWithinArray = 1;
                break;

        }
        
        for (int i = startingPointWithinArray; i < (numberOfApplicableAspects + startingPointWithinArray); i++) {
            
            this.signalLamps.put(this.getSignalType().returnApplicableSignalAspect()[i], true); // Create the Signal Lamp Map Entry.
            
        }
        
    }
    
    @Override
    public final void DisplayHighestAspect() {
        
        if (this.displayHighestAspect) {
            
            if (this.doNotStepHigherThan == null) {
                this.DisplayAspect(this.calculateBestAspect()); // Go for it! Show a proceed aspect, at the least restrictive aspect available.
            } else {
                this.DisplayAspect(this.doNotStepHigherThan);
            }
            
        } else {
            Map.Entry<SignalAspect, Boolean> lampMap = this.signalLamps.entrySet().iterator().next();
            this.DisplayAspect(lampMap.getKey()); // Show the lowest aspect available.
        }
        
    }

    @Override
    public Boolean getDisplayHighestAspect() {
        return displayHighestAspect;
    }

    @Override
    public void setDisplayHighestAspect(Boolean displayHighestAspect) {
        
        this.doNotStepHigherThan = null;
        this.displayHighestAspect = displayHighestAspect;
        this.DisplayHighestAspect();
    }
    
    @Override
    public void setDisplayHighestAspect(SignalAspect aspect) {
    
        this.doNotStepHigherThan = aspect;
        this.displayHighestAspect = true;
        this.DisplayHighestAspect();
        
    }

    @Override
    public void failSignalLamp(SignalAspect aspect) {
        
        if (this.signalLamps.containsKey(aspect)) {
            this.signalLamps.replace(aspect, false);
        }
        
    }

    @Override
    public void restoreSignalLamp(SignalAspect aspect) {
        
        if (this.signalLamps.containsKey(aspect)) {
            this.signalLamps.replace(aspect, true);
        }
        
    }

    @Override
    public Map<SignalAspect, Boolean> getSignalLampMap() {
        
        return this.signalLamps;
        
    }

    @Override
    public void DisplayAspect(SignalAspect aspect) {
        
        if (this.isAspectValid(aspect)) { // Validate the aspect for this signal type.
            
            SignalAspect newAspect;
            
            if (this.doNotStepHigherThan != null && !this.validateAspectAgainstNextAspect(this.doNotStepHigherThan)) {
                aspect = SignalAspect.RED;
            }

            switch (aspect) {
                
                case DOUBLE_YELLOW: // We need special consideration for a 4 aspect signal in connection with Double Yellow.
                    
                    if (!this.signalLamps.get(aspect)) { // Double Yellow Requested, Double Yellow Aspect Lamp showing false.
                        newAspect = SignalAspect.YELLOW; // Show a Yellow Aspect.
                    } else if (!this.signalLamps.get(SignalAspect.YELLOW)) { // Double Yellow Requested, Bottom Yellow (Yellow) Aspect Lamp showing false.
                        newAspect = SignalAspect.TOP_YELLOW; // Display only a TOP_YELLOW Signal Aspect.
                    } else {
                        newAspect = SignalAspect.DOUBLE_YELLOW; // Otherwise just display the DOUBLE_YELLOW.
                    }
                    break;
                    
                default: // Everything Else.

                    // Check if the requested aspect is listed in the SignalLamps Map, and check if the Lamp Value is set to false.
                    if (this.signalLamps.containsKey(aspect) && !this.signalLamps.get(aspect)) {
                        newAspect = SignalAspect.BLACK; // If so, this indicates the Lamp is out, show a Black Signal.
                    } else {
                        newAspect = aspect; // Otherwise, just show the Signal Aspect as desired.
                    }
            } 
         
            // If the aspect has changed, make sure that all signals in rear that need notification have been notified.
            if (this.currentAspect != newAspect) {
                
                this.currentAspect = newAspect;
                this.updateSignalsInRear(); // We only provide updates to Signals In Rear here to make sure that we dont send an update every time.
                
            }
            
        }
        
    }

    @Override
    public Boolean isAspectValid(SignalAspect aspect) {
        
        if (Arrays.toString(this.signalType.returnApplicableSignalAspect()).contains(aspect.toString())) {
            return true;
        } else {
            return false;
        }
        
    }

    @Override
    public Boolean validateAspectAgainstNextAspect(SignalAspect aspect) {
        
        int thisSignalAspect = 0;
        int nextSignalAspect = 0;
        
        for (int i = 0; i < SignalAspect.values().length; i++) {
            
            if (SignalAspect.values()[i].equals(aspect)) {
                thisSignalAspect = i;
            }
            
            if (SignalAspect.values()[i].equals(this.applicableSignalAspect)) {
                nextSignalAspect = i;
            }
            
            if (nextSignalAspect <= thisSignalAspect) {
                return true;
            }
            
        }
        
        return false;
    }

    @Override
    public String getFullSignalIdentity() {
        return String.format ("%s%s", this.getPrefix(), this.getIdentity());
    }

}

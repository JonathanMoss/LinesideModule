package com.jgm.lineside;

import java.util.Random;

/**
 * This class provides the physical representation of a train detection section.
 * This class represents both Track Circuit and Axle Counter forms of train detection.
 * @author Jonathan Moss
 * @version v1.0 September 2016
 */
public class TrainDetection {
    
    private final String identity; // The identity of the points (without the TC prefix.)
    private DetectionStatus detectionStatus; // The DetectionStatus (CLEAR or OCCUPIED).
    private FailureStatus failureStatus; // Member to hold the FailureStatus of the points.
    private final TD_Type type; // The type of train detection section - TC or AC.
    private Boolean shuntedTrack = false; // A flag to indicate if the TD Section has been 'shunted'.
    private Boolean trainOccupyingSection = false; // A flag to indication if a train is physically occupying the TD Section.
    private Boolean intermittentTrackCircuitFailure = false; // A flag to indicate if an intermittent track circuit failure has been set.
    
    /**
     * This is the constructor method for a Train Detection Object.
     * @param identity A <code>String</code> representing the identity of the points.
     * @param type A <code>TD_Type</code> object representing the type of Train Detection - Axle Counter or Track Circuit.
     */
    public TrainDetection(String identity, TD_Type type) {
        // Fill member variables with parameters passed / defaults.
        this.identity = identity;
        this.type = type;
        this.detectionStatus = DetectionStatus.CLEAR;
        this.failureStatus = FailureStatus.NORMAL;
    }
    
    /**
     * This method is used to simulate the technician 'shunting' the train detection section.
     * @param shunt A <code>Boolean</code> - <i>true</i> indicates shunt, <i>false</i> indicates clear.
     */
    public void tecShunt(Boolean shunt) {        
        if (this.validateCondition("tecShunt")) {
            if (shunt) {
                if (this.detectionStatus == DetectionStatus.CLEAR) {
                    this.detectionStatus = DetectionStatus.OCCUPIED;
                    this.shuntedTrack = true;
                    statusChange(this.detectionStatus.toString());
                }
            } else {
                if (this.shuntedTrack && this.failureStatus == FailureStatus.NORMAL) {
                    this.detectionStatus = DetectionStatus.CLEAR;
                    this.shuntedTrack = false;
                    statusChange(this.detectionStatus.toString());
                }
            }
        }  
    }
    
    /**
     * This method is called to simulate a train occupying the train detection section.
     * This method should only be called by the train movement simulator sub-system.
     */
    public void TrainOccupiesSection() {
        if (this.validateCondition("TrainOccupiesSection")) {
            this.trainOccupyingSection = true;
            if (this.failureStatus != FailureStatus.FAILED_CLEAR_WHEN_OCCUPIED) {
                this.detectionStatus = DetectionStatus.OCCUPIED;
            }
            statusChange(this.detectionStatus.toString());
        }
    }
    
    /**
     * This method is called to simulate a train leaving the train detection section.
     * This method should only be called by the train movement simulator sub-system.
     */
    public void TrainClearsSection() {
        if (this.validateCondition("TrainClearsSection")) {
            this.trainOccupyingSection = false;
            switch (this.type) {
                case AXLE_COUNTER:
                    if (this.failureStatus == FailureStatus.MIS_COUNT || this.failureStatus == FailureStatus.FAILED_OCCUPIED_WHEN_CLEAR) {
                        break;
                    } else {
                        if (!this.shuntedTrack) {this.detectionStatus = DetectionStatus.CLEAR; statusChange(this.detectionStatus.toString());}
                        break;
                    }
                case TRACK_CIRCUIT:
                    if (this.failureStatus == FailureStatus.FAILED_OCCUPIED_WHEN_CLEAR) {
                        break;
                    } else {
                        if (!this.shuntedTrack) {this.detectionStatus = DetectionStatus.CLEAR; statusChange(this.detectionStatus.toString());}
                        break;
                    }
            }
        }
    }
    public void setIntermittentTrackCircuitFailure() {
        if (this.validateCondition("setIntermittentTrackCircuitFailure")) {
            this.intermittentTrackCircuitFailure = true;
            new Thread(() -> {
                do {
                    try {
                        Random rand = new Random();
                        Thread.sleep(rand.nextInt(9999));
                        if (this.detectionStatus == DetectionStatus.CLEAR) {
                            this.detectionStatus = DetectionStatus.OCCUPIED;
                        } else {
                            if (!this.trainOccupyingSection) {
                                this.detectionStatus = DetectionStatus.CLEAR;
                            }
                        }
                            System.out.println(this.detectionStatus);
                        } catch (InterruptedException ex) {        
                    }
                } while (this.intermittentTrackCircuitFailure);    
            }).start(); 
        }
    }
    
    public void restoreIntermittentTrackCircuitFailure () {
        if (this.validateCondition("restoreIntermittentTrackCircuitFailure")) {
            this.intermittentTrackCircuitFailure = false;
            if (this.failureStatus == FailureStatus.NORMAL) {
                if (!this.trainOccupyingSection) {
                    this.detectionStatus = DetectionStatus.CLEAR;
                    statusChange(this.detectionStatus.toString());
                }
            }
        }
    }
    
    public Boolean validateCondition(String method) {
    // *****************************************************************************************************************************************************
    // *                            |Shunt  |Intermittent   |FAILED_CLEAR_WHEN_OCCUPIED |FAILED_OCCUPIED_WHEN_CLEAR |NORMAL |MIS_COUNT  |Occupied By Train *
    // * ------------------------------------------------------------------------------------------------------------------------------------------------- *
    // * Shunt                      |       |No             |No                         |No                         |Yes    |No         |No                *
    // * Intermittent*              |No     |               |No                         |No App, Yes Res            |Yes    |No         |No App, Yes Res   *
    // * FAILED_CLEAR_WHEN_OCCUPIED |No     |No             |                           |No                         |No     |No         |No                *
    // * FAILED_OCCUPIED_WHEN_CLEAR |No     |No App, Yes Res|No                         |                           |No     |No         |Yes               *
    // * NORMAL                     |Yes    |Yes            |No                         |No                         |       |No         |Yes               *
    // * MIS_COUNT                  |No     |No             |No                         |No                         |No     |           |Yes               *
    // * Occupied by Train          |No     |No App, Yes Res|No                         |Yes                        |Yes    |Yes        |                  *
    // *****************************************************************************************************************************************************
    // * = Track Circuit Only.

        switch (method) {
            case "setIntermittentTrackCircuitFailure":
                if (this.type != TD_Type.AXLE_COUNTER && this.failureStatus == FailureStatus.NORMAL && this.shuntedTrack == false) { return true; } else { return false; }
            case "restoreIntermittentTrackCircuitFailure":
                if (this.type != TD_Type.AXLE_COUNTER && (this.failureStatus == FailureStatus.NORMAL || this.failureStatus == FailureStatus.FAILED_OCCUPIED_WHEN_CLEAR) && this.shuntedTrack == false) { return true; } else { return false; }
            case "tecShunt":
                if (!this.intermittentTrackCircuitFailure && this.failureStatus == FailureStatus.NORMAL && !this.trainOccupyingSection) { return true; } else { return false; }
            case "TrainOccupiesSection":
                return true;
            case "TrainClearsSection":
                return true;
            default:
               return false; 
        }
    }
    
    /**
     * This method sets the failed status of the train detection status.\s
     * @param status A <code>FailureStatus</code> object representing the failure status of the train detection status.
     */
    public void setFailureStatus(FailureStatus status) {
    // **********************************************************************************************************
    // *                |  FAILED_CLEAR_WHEN_OCCUPIED   |   FAILED_OCCUPIED_WHEN_CLEAR  | NORMAL    | MIS_COUNT *
    // * ------------------------------------------------------------------------------------------------------ *
    // * Axle Counter   |           No                  |               Yes             |   Yes     |   Yes     *
    // * Track Circuit  |           Yes                 |               Yes             |   Yes     |   No      *
    // **********************************************************************************************************
        switch(status) {
            case FAILED_CLEAR_WHEN_OCCUPIED:
                if (this.type != TD_Type.AXLE_COUNTER) {
                    this.failureStatus = status;
                } 
                break;
            case MIS_COUNT:
                if (this.type != TD_Type.TRACK_CIRCUIT) {
                    this.failureStatus = status;
                }
                break;
            case FAILED_OCCUPIED_WHEN_CLEAR:
                this.detectionStatus = DetectionStatus.OCCUPIED;
            case NORMAL:
                this.failureStatus = status;
                break;
        }
        System.out.println(this.failureStatus);
    }
    
    /**
     * This method returns the detection status of the train detection section
     * @return A <code>DetectionStatus</code> object stating <i>OCCUPIED</i>, or <i>CLEAR</i>
     */
    public DetectionStatus getDetectionStatus () {
        return this.detectionStatus;
    }
       
    /**
     * This method updates the Remote Interlocking with any status changes.
     * @param message A <code></code> representing the message that should be passed to the remote interlocking.
     */
    public void statusChange(String message) {
        // Temporary!
        System.out.println(String.format("TC|%s|%s", this.identity, message));
    }
}

enum DetectionStatus {
    CLEAR, OCCUPIED;
}

enum FailureStatus {
    FAILED_CLEAR_WHEN_OCCUPIED, FAILED_OCCUPIED_WHEN_CLEAR, NORMAL, MIS_COUNT;
}

enum TD_Type {
    AXLE_COUNTER, TRACK_CIRCUIT;
}

package com.jgm.lineside;
import java.util.*;

/**
 * This class contains the blueprint for a single end of a set of points.
 * This class is the physical representation of the points, and does not contain any references to, or interlocking functionality.
 * @author Jonathan Moss
 * @version 1.0 15/08/2016
 */
public class Points {
    
    private final static HashMap <String, Integer> POINTS_HM = new HashMap <String, Integer>(); // Map to store each pointIndex and point Identity.
    private static int pointsTally = 0; // Keeping a tally on how many points objects have been created.
    private final String identity; // Store the String Variable of the points identity, e.g. "4076A"
    private PointsPosition positionOfPoints; // Store the position of the points, either Normal, Reverse or Unknown.
    private Boolean pointDetection; // Are the points detected (true) or otherwise (false).
    private Boolean arePointsSecured; // Are the points secured (true) or not (false) by a Clip and/or Scotch.
    private DetectionAvailable detectionAvailable; // What detection is available to the points, NORMAL_ONLY, REVERSE_ONLY, BOTH, NONE;
    private PointsPower pointsPower; // Is power available to the points; POWER = move under power, OFF_POWER = need to be wound manually.
    private int powerOperationSeconds = 5; // The time it takes for a set of points to move from one position to another under power operation.
    private int manualOperationSeconds = 100; // The time it takes for a set of points to be moved from one position to another manually.
    private Thread threadObject; // Used as a container for the thread that moves the points.
    private Boolean pointsAreMoving; // A flag to indicate true when the points are moving, otherwise false.
    
    /**
     * This method (Constructor) creates a points object and assigns the default properties.
     * @param identity a <code>String</code> that represents the textual identity of the points.
     */
    public Points(String identity) {
        
        // Set default field values.
        this.identity = identity; // Set the identity
        this.positionOfPoints = PointsPosition.NORMAL; // Sets the default position of the points as Normal.
        this.pointDetection = true; // The points are detected.
        this.arePointsSecured = false; // The points are not secured (i.e. no Clip/Scotch).
        this.detectionAvailable = DetectionAvailable.BOTH; // Detection is available in both Normal and Reverse
        this.pointsPower = PointsPower.POWER; // The points are operating under power.
        this.pointsAreMoving = false; // Sets the flag indicating that the points are not moving at this moment in time.
        
        // Add the instance to the static HashMap for indexing later.
        Points.POINTS_HM.put(this.identity, Points.pointsTally);
        Points.pointsTally ++; // Increase the count.
        
    }
    
    /**
     * This method sets the amount of seconds it takes for a set of points to move from one position to another.
     * The minimum acceptable value is 5 seconds, the maximum is 60 seconds.
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @param seconds An <code>integer</code> representing seconds.
     */
    public void setPowerOperationInterval(int seconds) {
        
        int maxValue = 60, minValue = 5;
        
        if (seconds < minValue) {
            
            this.powerOperationSeconds = minValue;
            
        } else if (seconds > maxValue) {
            
            this.powerOperationSeconds = maxValue;
            
        } else {
            
            this.powerOperationSeconds = seconds;
            
        }
    
    }
    
    /**
     * This method is used to set what detection is available to a particular set of points.
     * @param detectionAvailable a <code>DetectionAvailable</code> constant, specifying <i>NORMAL_ONLY, REVERSE_ONLY, NONE, BOTH</i>
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @see getDetectionAvailable
     * @see getDetectionStatus
     * @see setDetectionStatus
     */
    public void setDetectionAvailable(DetectionAvailable detectionAvailable) {
        
        this.detectionAvailable = detectionAvailable;
        
    }
    
    /**
     * This method returns what detection is available to the points.
     * @return <code>DetectionAvailable</code> object specifying <i>NORMAL_ONLY, REVERSE_ONLY, NONE, BOTH</i>
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @see setDetectionAvailable
     * @see getDetectionStatus
     * @see setDetectionStatus
     */
    public DetectionAvailable getDetectionAvailable() {
        
        return this.detectionAvailable;
        
    }
    
    /**
     * Calling this method attempts to obtain detection based on the status of the points.
     * 
     * This method is required by the Technicians Interface and methods within this package.
     * 
     */
    public void attemptDetection() {
        
        switch (this.detectionAvailable) {
            case NORMAL_ONLY:
                if (this.positionOfPoints == PointsPosition.NORMAL) {
                    this.setDetectionStatus(true);
                } else {
                    this.setDetectionStatus(false);
                }
            break;
            
            case REVERSE_ONLY:
                if (this.positionOfPoints == PointsPosition.REVERSE) {
                    this.setDetectionStatus(true);
                } else {
                    this.setDetectionStatus(false);
                }
            break;
            
            case BOTH:
                if (this.positionOfPoints == PointsPosition.NORMAL || this.positionOfPoints == PointsPosition.REVERSE) {
                    this.setDetectionStatus(true);
                } else {
                    this.setDetectionStatus(false);
                }
            break;
            
            case NONE:
                this.setDetectionStatus(false);
            break;
        }
        
    }
    
    /**
     * This method sets the points moving flag, as appropriate. It is used to prevent multiple calls to the same set of points.
     * @param arePointsMoving <code>BOOLEAN</code> indicating <i>true</i> where the points are moving, <i>false</i> where the points are not moving.
     * 
     * This method is required by this class, and methods within this package only.
     * 
     */
    protected void setPointsAreMoving (Boolean arePointsMoving) {
        
        this.pointsAreMoving = arePointsMoving;
        
    }
    
    /**
     * This method is used to request the points to be moved to a particular position - either Normal or Reverse.
     * @param toPosition a <code>PointsPosition</code> object <code>constant</code>, either <code>NORMAL</code> <i>or</i> <code>REVERSE</code>
     * 
     * This method is required by the Technicians Interface and Interlocking Components.
     * 
     */
    public void movePointsUnderPower (PointsPosition toPosition) {
        
        // Check of the points are already moving?
        if (!this.pointsAreMoving)  { // Points are not moving.
            // Check if the points are operating under power.
            if (this.pointsPower == PointsPower.POWER) { // Points are operating under power.
                // Check if the points need moving (i.e. are in a different position)
                if (this.positionOfPoints != toPosition) { // The points are in a different position.
                    // Check if the points are secured
                    if (!this.arePointsSecured) { // The points are not secured.
                        // Move Points...

                            if (this.threadObject == null || this.threadObject.getState() == Thread.State.TERMINATED) {
                                this.setPointsAreMoving(true);
                                this.threadObject = new pointsMovingPower(this, toPosition, this.powerOperationSeconds);
                                this.threadObject.start();
                            } else {
                                this.sendMessage("Cannot process last request to move the points...");
                            }

                    } else {

                         this.setDetectionStatus(false); // The points are secured; lose detection.

                    }

                } else { // The points are already in the required position.

                    this.attemptDetection(); // Attempt detection; the points are under power, and do not need moving.

                }

            } else { // Points are not operating under power.

                if (this.getPointsPosition().equals(toPosition)) {
                    
                    this.attemptDetection();
                    
                } else {
                    
                    this.setDetectionStatus(false); // Lose detection.
                    
                }
                

            }
            
        } else { // Points are already moving.
            
            this.sendMessage("Cannot process last request to move the points...");
            
        }
    }
    
    /**
     * This is a temporary method that is used to display the output during production; will be replaced by a specific message flow method
     * that informs the interlocking (which in turn, will notify the Display Module.
     * @param message a <code>String</code> that is printed out to the console.
    */
    protected void sendMessage(String message) {

        System.out.println(message);
        
    }
    
    /**
     * Where Points objects are instantiated within an array, this method returns the associated index integer (Assuming this is the case).
     * Further, to be effective, all points should be instantiated within a single array.
     * If no match can be found, then the integer 9999 is returned.
     * 
     * This method is required by the Technicians Interface and Interlocking Components.
     * 
     * @param identity a <code>String</code> indicating the identity of the points.
     * @return <code>integer</code> representing the array index of the Points object within the array.
     */
    public static int returnPointIndex(String identity) {
        
        return Points.POINTS_HM.get(identity);
            
    }

    /**
     * This method returns the identity of the points object.
     * @return <code>String</code> representing the identity of the points.
     * 
     * This method is required by the Technicians Interface and Interlocking Components.
     * 
     */
    public String getIdentity() {

        return this.identity;
        
    }
    
    /**
     * This method sets the power status of the points - in other words, the power arrangements.
     * @param pointsPower a <code>PointsPower</code> constant. <i>POWER</i> indicates that the points motors are able to move the points, <i>OFF_POWER</i> indicates that the
     * points are required to be operated manually.
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @see getPointsPower
     */
    protected void setPointsPower(PointsPower pointsPower) {
        
        this.pointsPower = pointsPower;
        
    }
    
    /**
     * This method returns the status of the points power - in other words, the power arrangements.
     * @return <code>PointsPower</code> <i>POWER</i> indicates that the points motors are able to move the points, <i>OFF_POWER</i> indicates that the
     * points are required to be operated manually.
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @see setPointsPower
     */
    public PointsPower getPointsPower() {
        
        return this.pointsPower;
    }
    
    /**
     * This method provides a means to specify if the points are secured.
     * 'Points Secured' refers to the act of applying a clip and scotch, or scotch only to the points.
     * A clip and scotch are mechanical means to prevent points being moved either manually or under power operation, and
     * are applied by a person known as a points operator.
     * 
     * This method is required by the Technicians Interface only.
     * 
     * @param arePointsSecured <code>BOOLEAN</code> <i>true</i> indicating that the points are secured, <i>false</i> otherwise.
     * @see getPointsSecured
     */
    protected void setPointsSecured (Boolean arePointsSecured) {
        
        this.arePointsSecured = arePointsSecured;
        
    }
    
    /**
     * This method returns a value indicating if the points are secured by either a clip and scotch, or scotch only.
     * @return <code>BOOLEAN</code> indicating that the points are not secured <i>false</i> or otherwise <i>true</i>
     * 
     * This method is required by the Technicians Interface Only.
     * 
     * @see setPointsSecured
     */
    public Boolean getPointsSecured () {
        
        return this.arePointsSecured;
        
    }
    
    /**
     * This method returns the current position of the points.
     * @return <code>PointsPosition</code> Specifies that the points are either <i>NORMAL, REVERSE, </i>or <i>UNKNOWN</i>
     * 
     * This method is required by the Technicians Interface and Interlocking Components.
     * 
     * @see setPointsPosition
     */
    public PointsPosition getPointsPosition() {
        
        return this.positionOfPoints;
        
    }
    
     /**
     * This method changes the value of PositionOfPoints property; it should only be called by a <code>pointsMovingPower</code> object.
     * @param toPosition A <code>PointsPosition</code> value as either <i>NORMAL, REVERSE, </i>or <i>UNKNOWN</i>
     * 
     * This method is required by this package only.
     * 
     * @see getPointsPosition
     */
    protected void setPointsPosition(PointsPosition toPosition) {
        
        this.positionOfPoints = toPosition;
        
    }
    
    /**
     * This method returns the current detection status of the points.
     * @return <code>BOOLEAN</code> Indicates that the points are detected <i>true</i> or where the points have no detection <i>false</i>.
     * 
     * This method is required by the Interlocking Components and Technicians Interface.
     * 
     * @see setDetectionStatus
     */
    public Boolean getDetectionStatus() {
        
        return this.pointDetection;
        
    }
    
    /**
     * This method sets whether or not, the points have detection.
     * @param detectionStatus <code>BOOLEAN</code> that determines whether or not the points are detected - <i>true</i> or otherwise <i>false</i>
     * This method should only be called from within the attemptDetection() method if a true value is being passed.
     * 
     * This method is required by this class only.
     * 
     * @see getDetectionStatus
     */
    private void setDetectionStatus(Boolean detectionStatus) {
        
        this.pointDetection = detectionStatus;
        
    }
    
    /**
     * This is a PUBLIC method, to be used to remove detection. Detection is reinstated by calling attemptDetection();
     */
    public void dropDetection () {
        
        this.setDetectionStatus(false);
        
    }
}

/**
 * This enumeration specifies the 3 valid values concerning the position of the points.
 * NORMAL, REVERSE - Standard terminology for the position (orientation) of the points - required by signalling control tables.
 * UNKNOWN - This position indicates that the points are neither Normal nor Reverse.
 */
enum PointsPosition {
    
    NORMAL, REVERSE, UNKNOWN;
     
}

/**
 * This enumeration specifies the 4 valid values concerning what detection is available on a particular set of points.
 * NORMAL_ONLY - Only when the points are sitting in the normal position is detection available;
 * REVERSE_ONLY - Only when the points are sitting in the reverse position is detection available.
 * BOTH - Detection is available in both normal and reverse.
 * NONE - Detection is not available in any orientation.
 */
enum DetectionAvailable {
    
    NORMAL_ONLY, REVERSE_ONLY, BOTH, NONE;
    
}

/**
 * This enumeration specifies the 2 valid values concerning points power.
 * POWER - points are operating under power operation (able to move under power of the motor);
 * OFF_POWER - points are not able to move under power operation and will require manual operation. 
 */
enum PointsPower {
    
    POWER, OFF_POWER;
    
}




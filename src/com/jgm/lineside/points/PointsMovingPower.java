package com.jgm.lineside.points;

import com.jgm.lineside.LineSideModule;

/**
 * This class is a blueprint for a threaded object that moves the points from one position to another. 
 * @author Jonathan Moss
 * @version 1.0 17/08/2016
 */
public class PointsMovingPower extends Thread {
    
    // Declare fields.
    private final Points pointObject; // A reference to the instantiated point object.
    private final PointsPosition toPosition; // The position that the points are required to move to.
    private final int secondsToMotor; // How long it takes the points to move from position A to position B.
    private final String identity; // The identity of the points.
    
    /**
     * The constructor method for the pointsMovingPower class. Running this method creates a thread object that simulates the moving
     * of points from 1 position to another.
     * @param pointObject A reference to a <code>Points</code> object.
     * @param toPosition A <code>PointsPosition</code> value that determines the required position of the points.
     * @param secondsToMotor an <code>integer</code> specifying the time (in seconds) how long the points take to operate from A to B.
     */
   protected PointsMovingPower (Points pointObject, PointsPosition toPosition, int secondsToMotor) {
        
        // Assign values to field variables.
        this.pointObject = pointObject;
        this.toPosition = toPosition;
        this.secondsToMotor = secondsToMotor;
        
        // Get the identity of the points from the reference to the points object.
        this.identity = this.pointObject.getIdentity();
        
    }
    
    @Override
    public void run() {
        LineSideModule.sendUpdatePoints(this.pointObject);
        this.pointObject.dropDetection(); // Remove detection.
        this.pointObject.setPointsPosition(PointsPosition.UNKNOWN); // Move the points to unknown.
        LineSideModule.sendUpdatePoints(this.pointObject);
        try {
            Thread.sleep(this.secondsToMotor * 1000); // Simulate the points taking time to traverse from position A to position B.
        } catch (InterruptedException ie) {}
        this.pointObject.setPointsPosition(toPosition); // Show the points in the new position.
        this.pointObject.attemptDetection(); // Attempt to gain detection.
        this.pointObject.setPointsAreMoving(false); // Show the points as having finished moving.
        LineSideModule.sendUpdatePoints(this.pointObject);
    }
}

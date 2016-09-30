package com.jgm.lineside.points;
/**
 * This enumeration specifies the 3 valid values concerning the position of the points.
 * NORMAL, REVERSE - Standard terminology for the position (orientation) of the points - required by signalling control tables.
 * UNKNOWN - This position indicates that the points are neither Normal nor Reverse.
 */
public enum PointsPosition {
    
    NORMAL, REVERSE, UNKNOWN;
     
}

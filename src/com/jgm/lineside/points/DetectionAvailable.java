package com.jgm.lineside.points;

/**
 * This enumeration specifies the 4 valid values concerning what detection is available on a particular set of points.
 * NORMAL_ONLY - Only when the points are sitting in the normal position is detection available;
 * REVERSE_ONLY - Only when the points are sitting in the reverse position is detection available.
 * BOTH - Detection is available in both normal and reverse.
 * NONE - Detection is not available in any orientation.
 */
public enum DetectionAvailable {
    NORMAL_ONLY, REVERSE_ONLY, BOTH, NONE;
}

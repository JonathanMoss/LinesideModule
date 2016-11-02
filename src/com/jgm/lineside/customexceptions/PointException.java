package com.jgm.lineside.customexceptions;
/**
 * The class PointException is a form of Exception that indicates conditions that a reasonable application might want to catch.
 * <p>
 * This Custom Exception is used to catch the condition where conditions concerning the DataLogger are invalid or cause an error.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class PointException extends Exception{

    /**
     * Constructs a new exception with the specified detail message.
     * @param message <code>String</code>  the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public PointException (String message) {
        super(message);
    }
    
}

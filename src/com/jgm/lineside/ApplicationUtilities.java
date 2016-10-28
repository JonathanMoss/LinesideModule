package com.jgm.lineside;

/**
 * This Class contains various ApplicationUtilities and Helper methods used in the LineSide Module.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class ApplicationUtilities {

    private static final String NEW_LINE = System.lineSeparator(); // The OS Specific Line Separator.
    private static final String OPERATING_SYSTEM = System.getProperty("os.name"); // A String containing the Operating System designation.

    /**
     * This method returns a String containing the Operating System designation, as referenced in the JVM.
     * @return <code>String</code> Containing the JVM Operating System designation.
     */
    private static String getOperatingSystemName() {
        return OPERATING_SYSTEM;
    }
    
    /**
     * This method returns a String representing the OS Specific 'Line Separator' character.
     * @return <code>String</code> Containing the OS Specific Line Separator character(s).
     */
    public static String getNewLine() {
        return NEW_LINE;
    }
    
    /**
     * This method returns a string representing 'OK' for display on the command line.
     * 
     * This method determines an appropriate indication based on the capabilities of the console and the underlying OS.
     * 
     * @return A <code>String</code> representing 'OK', or a check mark.
     */
    public static String getOK() {
        
        if (OPERATING_SYSTEM.contains("Windows")) {
            return "OK";
        } else {
            return "[\u2713]";
        }
        
    }
    
     /**
     * This method returns a string representing 'FAILED' for display on the command line.
     * 
     * This method determines an appropriate indication based on the capabilities of the console and the underlying OS.
     * 
     * @return A <code>String</code> representing 'FAILED', or a Cross.
     */
    public static String getFailed() {
        
        if (OPERATING_SYSTEM.contains("Windows")) {
            return "FAILED";
        } else {
            return "[\u2717]";
        }
        
    }
    
    
}

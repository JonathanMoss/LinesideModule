package com.jgm.lineside;

import static com.jgm.lineside.LineSideModule.attemptDataLoggerConnection;
import static com.jgm.lineside.LineSideModule.buildPoints;
import static com.jgm.lineside.LineSideModule.obtainDataLoggerConnectionDetails;
import static com.jgm.lineside.LineSideModule.obtainRemoteInterlockingDetails;
import static com.jgm.lineside.LineSideModule.validateCommandLineArguments;

/**
 * This Class provides the 'Start-up Script' for a LineSide Module.
 * 
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public abstract class Initialise {

    public static void runStartUpScript() throws Exception{
        
        validateCommandLineArguments();
        //obtainRemoteInterlockingDetails();
        obtainDataLoggerConnectionDetails();
        attemptDataLoggerConnection();
        //buildPoints();
    
    }
    
}

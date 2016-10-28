package com.jgm.lineside;

/* 
 * LineSideModule : a Java Line Side Module implementation for a UK Railway 
 *                  Signalling Simulator.
 * 
 * (C) Copyright 2016, by Jonathan Moss and Contributors.
 *
 * Project Info:  http://www.jgm-net.co.uk/simulator/line_side_module/index.html
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 *  Other names may be trademarks of their respective owners.]
 *
 * -----------------------
 * LineSideModule.java
 * -----------------------
 * (C) Copyright 2016, by Jonathan Moss and Contributors.
 *
 * Original Author:  Jonathan Moss (for JGM-NET.co.uk);
 * Contributor(s):   TBC.
 *
 * Changes:
 * --------
 * 01-Aug-2016 : Version 1.0 published;
 *
 */

import com.jgm.lineside.database.MySqlConnect;
import static com.jgm.lineside.ApplicationUtilities.getFailed;
import static com.jgm.lineside.ApplicationUtilities.getOK;
import com.jgm.lineside.traindetection.TrainDetection;
import com.jgm.lineside.traindetection.TrainDetectionType;
import com.jgm.lineside.datalogger.Colour;
import com.jgm.lineside.datalogger.DataLoggerClient;
import com.jgm.lineside.interlocking.MessageHandler;
import com.jgm.lineside.interlocking.MessageType;
import com.jgm.lineside.interlocking.RemoteInterlockingClient;
import com.jgm.lineside.points.Points;
import com.jgm.lineside.signals.ControlledSignal;
import com.jgm.lineside.signals.Signal;
import com.jgm.lineside.signals.SignalAspect;
import com.jgm.lineside.signals.SignalType;
import customexceptions.DataLoggerException;
import customexceptions.InvalidCommandLineArgument;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides LineSide Module Functionality.
 * 
 * @author Jonathan Moss
 * @version 1.0 - August 2016
 */
public class LineSideModule {
    
    /**
     * The permitted length of the LineSideModule Identity.
     */
    private static final int MODULE_IDENTITY_LENGTH = 5;
    /**
     * A String representation of the Identity of this LineSide Module.
     */
    private static String lsmIdentity;
    
    /**
     * The DataBase index key of this LineSide Module.
     */
    private static int lsmIndexKey;
    
    /**
     * The version number of this software.
     */
    private static final double VERSION = 1.0;
    
    /**
     * The version date of this software.
     */
    private static final String VERSION_DATE = "27/10/2016";
    
    /**
     * The RemoteInterlocking Connection Object.
     */
    public static RemoteInterlockingClient remoteInterlocking;
    
    /**
     * A String representation of the Identity of the Remote Interlocking.
     */
    private static String riIdentity;
    
    /**
     * The DataBase index key of the Remote Interlocking.
     */
    private static int riIndexKey;
    
    /**
     * The IP address of the Remote Interlocking server.
     */
    private static String riHost;
    
    /**
     * The port number of the Remote Interlocking Server.
     */
    private static String riPort;

    /**
     * The DataLogger Connection Object.
     */
    public static DataLoggerClient dataLogger;
    
    /**
     * The IP address of the DataLogger server.
     */
    private static String dlHost;
    
    /**
     * The port number of the DataLogger Server.
     */
    private static String dlPort;

    /**
     * ArrayList to hold all Points Objects.
     */
    private static final ArrayList <Points> POINTS_ARRAY = new ArrayList <>();
    
    /**
     * ArrayList to hold all Signal Objects.
     */
    private static final ArrayList <Signal> SIGNAL_ARRAY = new ArrayList<>();
    
    /**
     * ArrayList to hold all Train Detection Section Objects.
     */
    private static final ArrayList <TrainDetection> TRAIN_DETECTION_ARRAY = new ArrayList<>();
    
    /**
     * The ResultSet that is populated when a SELECT query is sent to the Remote DB.
     */
    private static ResultSet rs; 
    
    /**
     * A String array to hold the Command Line Arguments.
     */
    private static String[] commandLineArguments;

    /**
     * The Main Method (entry point) of the LineSideModule Class.
     * <p>
     * The LineSideModule Identity must be passed on the command line as a parameter, otherwise the LineSideModule will not run!!
     * 
     * @param args <code>String</code> Arguments passed to the command line.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, Exception {
       
        commandLineArguments = args;
        
        dataLogger = new DataLoggerClient();
        dataLogger.setName("DataLoggerThread");
        dataLogger.start();
        
        System.out.println(String.format ("LineSide Module v%s (%s) - Running startup script...", VERSION, VERSION_DATE));
        System.out.println("-------------------------------------------------------------\n");
        
        try {
            
            Initialise.runStartUpScript();
            
        } catch (InvalidCommandLineArgument ex) {
            
            dataLogger.sendToDataLogger(String.format ("%s%s '%s'%s", 
                Colour.RED.getColour(), getFailed(), ex.getMessage(), Colour.RESET.getColour()), true, true);
            exitCommandLine();
 
        } catch (DataLoggerException ex) {
            dataLogger.sendToDataLogger(String.format ("%s%s '%s'%s", 
                Colour.RED.getColour(), getFailed(), ex.getMessage(), Colour.RESET.getColour()), true, true);
        }
        
       
                      

          

        
            
//        // 7) Build the Controlled Signals.
//            dataLogger.sendToDataLogger("Connected to remote DB - looking for Controlled Signals assigned to this LineSide Module...", true, false);
//            try {
//                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM `Controlled_Signals` WHERE `parentLineSideModule` = %d;", lsmIndexKey));
//                int recordsReturned = 0;
//                while (rs.next()) {
//                    try {
//                        SIGNAL_ARRAY.add(new ControlledSignal(rs.getString("prefix"), rs.getString("identity"), SignalType.valueOf(rs.getString("type"))));
//                        recordsReturned ++;
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                        true, true);
//                    exitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
//                        Colour.RED.getColour(), Colour.RESET.getColour()));
//                    }
//                }
//                if (recordsReturned == 0) {
//                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                        true, true);
//                    exitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
//                        Colour.RED.getColour(), Colour.RESET.getColour()));
//                }
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
//                    true,true);
//                System.out.println();
//                dataLogger.sendToDataLogger(String.format ("%s%-19s%-19s%-10s%s",
//                    Colour.BLUE.getColour(), "Controlled Signal", "Type", "Current Aspect", Colour.RESET.getColour())
//                    , true, true);
//                dataLogger.sendToDataLogger(String.format ("%s----------------------------------------------------%s",
//                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
//                    true, true);
//                for (int i = 0; i < SIGNAL_ARRAY.size(); i++) {
//                    dataLogger.sendToDataLogger(String.format("%s%-19s%-19s%-10s%s", 
//                        Colour.BLUE.getColour(), SIGNAL_ARRAY.get(i).getFullSignalIdentity(), SIGNAL_ARRAY.get(i).getSignalType().toString(), 
//                        (SIGNAL_ARRAY.get(i).getCurrentAspect() == SignalAspect.RED) ? Colour.RED.getColour() + SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.RESET.getColour() : SIGNAL_ARRAY.get(i).getCurrentAspect().toString(), 
//                        Colour.RESET.getColour()), 
//                        true, true);
//                }
//                System.out.println();
//            } catch (SQLException ex) {
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                    true, true);
//                exitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
//                    Colour.RED.getColour(), Colour.RESET.getColour()));
//            }
//            
//        // 8) Build the non-controlled signals.
//            dataLogger.sendToDataLogger("Connected to remote DB - looking for non-controlled Signals assigned to this LineSide Module...", true, false);
//            try {
//                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Non_Controlled_Signals WHERE parentLineSideModule = %d;", lsmIndexKey));
//                int recordsReturned = 0;
//                while (rs.next()) {
//                    try {
//                        AUTOMATIC_SIGNAL_ARRAY.add(new AutomaticSignal(rs.getString("prefix"), rs.getString("identity"), 
//                            AutomaticSignalType.valueOf(rs.getString("type")), rs.getString("line"), rs.getString("read_direction"), 
//                            Function.valueOf(rs.getString("function")), rs.getString("applicable_Signal")));
//                        recordsReturned ++;
//                    } catch (Exception ex) {
//                        dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                            Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                            true, true);
//                        exitCommandLine(String.format ("%sERROR: Cannot obtain Automatic Signal details from the database.%s",
//                            Colour.RED.getColour(), Colour.RESET.getColour()));
//                    }
//                }
//                if (recordsReturned == 0) {
//                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                        true, true);
//                    exitCommandLine(String.format ("%sERROR: Cannot obtain Automatic Signal details from the database.%s",
//                        Colour.RED.getColour(), Colour.RESET.getColour()));
//                }
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
//                    true,true);
//                System.out.println();
//
//                dataLogger.sendToDataLogger(String.format ("%s%-22s%-18s%-16s%s%s",
//                    Colour.BLUE.getColour(), "Automatic Signal", "Type", "Current Aspect", "Function", Colour.RESET.getColour()), 
//                    true, true);
//                dataLogger.sendToDataLogger(String.format ("%s------------------------------------------------------------------------%s",
//                    Colour.BLUE.getColour(), Colour.RESET.getColour()),
//                    true, true);
//                for (int i = 0; i < AUTOMATIC_SIGNAL_ARRAY.size(); i++) {
//                    dataLogger.sendToDataLogger(String.format("%s%-22s%-18s%-26s%s%s", 
//                        Colour.BLUE.getColour(), AUTOMATIC_SIGNAL_ARRAY.get(i).getSignalIdentity(),AUTOMATIC_SIGNAL_ARRAY.get(i).getType(), 
//                        (AUTOMATIC_SIGNAL_ARRAY.get(i).getCurrentAspect().toString().contains("YELLOW") || AUTOMATIC_SIGNAL_ARRAY.get(i).getCurrentAspect().toString().contains("WARNING"))? Colour.YELLOW.getColour() + AUTOMATIC_SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.BLUE.getColour() : Colour.RED.getColour() + AUTOMATIC_SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.BLUE.getColour(), 
//                        AUTOMATIC_SIGNAL_ARRAY.get(i).getFunction(), Colour.RESET.getColour()), 
//                        true, true);
//                }
//                System.out.println();
//            } catch (SQLException ex) {
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                    true, true);
//                exitCommandLine(String.format ("%sERROR: Cannot obtain Automatic Signal details from the database.%s",
//                    Colour.RED.getColour(), Colour.RESET.getColour()));
//            }
//            
//        // 9) Build the Train Detection Sections.
//            dataLogger.sendToDataLogger("Connected to remote DB - looking for Train Detection Sections assigned to this LineSide Module...", true, false);
//            try {
//                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Train_Detection WHERE parentLineSideModule = %d;", lsmIndexKey));
//                int recordsReturned = 0;
//                while (rs.next()) {
//                    try {
//                        TRAIN_DETECTION_ARRAY.add(new TrainDetection(rs.getString("identity"), TrainDetectionType.valueOf(rs.getString("type"))));
//                        recordsReturned ++;
//                    } catch (Exception ex) {
//                        dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                            Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                            true, true);
//                        exitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
//                            Colour.RED.getColour(), Colour.RESET.getColour()));
//                    }
//                }
//                if (recordsReturned == 0) {
//                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                        true, true);
//                     exitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
//                        Colour.RED.getColour(), Colour.RESET.getColour()));
//                }
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
//                    true,true);
//                System.out.println();
//                dataLogger.sendToDataLogger(String.format ("%s%-26s%-18s%s%s",
//                    Colour.BLUE.getColour(), "Train Detection Section", "Type", "Status", Colour.RESET.getColour()), 
//                    true, true);
//                dataLogger.sendToDataLogger(String.format ("%s--------------------------------------------------%s",
//                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
//                    true, true);
//                for (int i = 0; i < TRAIN_DETECTION_ARRAY.size(); i++) {
//                    dataLogger.sendToDataLogger(String.format("%s%-26s%-18s%-10s%s", 
//                        Colour.BLUE.getColour(), TRAIN_DETECTION_ARRAY.get(i).getIdentity(), TRAIN_DETECTION_ARRAY.get(i).getType().toString(),
//                        (TRAIN_DETECTION_ARRAY.get(i).getDetectionStatus().toString().contains("CLEAR"))? Colour.GREEN.getColour() + "CLEAR" + Colour.RESET.getColour() : Colour.RED.getColour() + "OCCUPIED" + Colour.RESET.getColour(), 
//                        Colour.RESET.getColour()), 
//                        true, true);
//                }
//                System.out.println();
//            } catch (SQLException ex) {
//                dataLogger.sendToDataLogger(String.format ("%s%s%s",
//                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
//                    true, true);
//                 exitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
//                    Colour.RED.getColour(), Colour.RESET.getColour()));
//            }
//        
//        // 10) Open a connection to the Remote Interlocking.
//            dataLogger.sendToDataLogger("Attempt a connection with the Remote Interlocking...", true, false);
//            remoteInterlocking = new RemoteInterlockingClient(riHost, Integer.parseInt(riPort));
//            remoteInterlocking.setName("RemoteInterlockingClient");
//            remoteInterlocking.start();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException ex) {
//
//            }
//            
//  
            
            //System.out.println();
            
        // 11) Wait for State Changes or Messages From the Remote Intelocking.
        //TODO

     
  
    }
    
    /**
     * This method is called when there is a problem and the program cannot continue.
     * It displays a message, and exits the program.
     */
    public static void exitCommandLine() {

        dataLogger.sendToDataLogger(String.format ("%sFatal Error: LineSideModule cannot continue",
                Colour.RED.getColour()), true, true);
        System.exit(0);
        
    }
    
    /**
     * This Method sends a message to the Remote Interlocking regarding a Points object
     * @param points <code>Points</code> object that requires an update sending to the Remote Interlocking.
     */
    public static void sendUpdatePoints (Points points) {
        
        MessageHandler.addOutgoingMessageToStack(MessageType.STATE_CHANGE, String.format ("POINTS.%s.%s.%s",
            points.getIdentity(), points.getPointsPosition(), points.getDetectionStatus()));
        
    }
    
    /**
     * This Method sends a message to the Remote Interlocking regarding a Controlled Signal object
     * @param signal <code>ControlledSignal</code> object that requires an update sending to the Remote Interlocking.
     */
    public static void sendUpdateControlledSignal (ControlledSignal signal) {
        
//        MessageHandler.addOutgoingMessageToStack(MessageType.STATE_CHANGE, String.format ("CONTROLLED_SIGNAL.%s.%s.%s",
//            signal.getPrefix(), signal.getId(), signal.getCurrentAspect().toString()));
        
    }
    
    /**
     * This Method sends a message to the Remote Interlocking regarding an Automatic Signal object
     * @param signal <code>AutomaticSignal</code> object that requires an update sending to the Remote Interlocking.
     */
//    public static void sendUpdateAutomaticSignal (AutomaticSignal signal) {
//        
//        MessageHandler.addOutgoingMessageToStack(MessageType.STATE_CHANGE, String.format ("AUTOMATIC_SIGNAL.%s.%s.%s",
//            signal.getPrefix(), signal.getId(), signal.getCurrentAspect().toString()));
//        
//    }
    
    /**
     * This Method sends a message to the Remote Interlocking regarding a Train Detection Section object
     * @param section <code>TrainDetection</code> object that requires an update sending to the Remote Interlocking.
     */
    public static void sendUpdateTrainDetection (TrainDetection section) {
        
        MessageHandler.addOutgoingMessageToStack(MessageType.STATE_CHANGE, String.format ("TRAIN_DETECTION.%s.%s",
            section.getIdentity(), section.getDetectionStatus()));
        
    }
    
    /**
     * This method sends a status update for all assets to the Remote Interlocking.
     */
    public static void sendUpdateAll() {
        
        // Points
        for (int i = 0; i < POINTS_ARRAY.size(); i++) {
            sendUpdatePoints(POINTS_ARRAY.get(i));
        }

        // Controlled Signals
//        for (int i = 0; i < SIGNAL_ARRAY.size(); i++) {
//            sendUpdateControlledSignal(SIGNAL_ARRAY.get(i));
//        }

        // Automatic Signals
//        for (int i = 0; i < AUTOMATIC_SIGNAL_ARRAY.size(); i++) {
//            sendUpdateAutomaticSignal(AUTOMATIC_SIGNAL_ARRAY.get(i));
//        }

        // Train Detection Sections
        for (int i = 0; i < TRAIN_DETECTION_ARRAY.size(); i++) {
            sendUpdateTrainDetection(TRAIN_DETECTION_ARRAY.get(i));
        }
        
    }
    
    /**
     * This method receives and actions a request to operate a set of points to the required position.
     * 
     * Note: Compliance with the request is not guaranteed; the only guarantee is that an attempt shall be made to move the points
     * to the required position and a further attempt shall be made to obtain detection.
     * 
     * @param identity <code>String</code> containing the identity of the points.
     * @param requestedPosition <code>PointsPosition</code> the requested position of the points.
     */
//    public static synchronized void incomingPointsRequest (String identity, PointsPosition requestedPosition) {
//    
//        POINTS_ARRAY.get(Points.returnPointIndex(identity)).movePointsUnderPower(requestedPosition);
//         
//    }
    
    /**
     * This method receives and actions a request to display a particular aspect at a Controlled Signal.
     * 
     * Note: Compliance with the request is not guaranteed; the only guarantee is that an attempt shall be made to show the requested
     * Signal Aspect.
     * 
     * @param prefix <code>String</code> The prefix of the Signal.
     * @param identity <code>String</code> The identity of the Signal.
     * @param requestedAspect <code>SignalAspect</code> The requested aspect.
     */
    public static synchronized void incomingControlledSignalRequest (String prefix, String identity, SignalAspect requestedAspect) {
    
//        SIGNAL_ARRAY.get(ControlledSignal.returnControlledSignalIndex(String.format ("%s%s", 
//            prefix, identity))).requestSignalAspect(requestedAspect);
        
    }
    
    /**
     * 
     * @param prefix
     * @param identity
     * @param requestedAspect 
     */
    public static synchronized void incomingAutomaticSignalRequest (String prefix, String identity, SignalAspect requestedAspect) {
        
    }
    
    public static String getRiIdentity() {
        return riIdentity;
    }

    
    
  
   

    
    
    

    
 
    /**
     * This method returns a reference to the ArrayList that holds the 
     * @return 
     */
    public static ArrayList getPointsArray () {
        return POINTS_ARRAY;
    }
    
    public static String getLineSideModuleIdentity() {
        return lsmIdentity;
    }
    
    /**
     * This method obtains the LineSide Module Identity from the Command Line Arguments, and validates it against the DataBase.
     * 
     * This method requires that the command line arguments have been passed to the correct String array prior to calling this method.
     * @throws customexceptions.InvalidCommandLineArgument
     */
    protected static void validateCommandLineArguments() throws InvalidCommandLineArgument{
    
        dataLogger.sendToDataLogger("Validating Command Line arguments...", true, false);
        
        if (commandLineArguments.length > 0) { // Make sure at least 1 argument was passed.
            
            if (commandLineArguments[0].length() == MODULE_IDENTITY_LENGTH) { // We are expecting a String of 5 characters only.
                
                lsmIdentity = commandLineArguments[0]; // Set this LineSide Module Identity.
                
                try {

                    rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM `Lineside_Module` WHERE `identity` = '%s';", lsmIdentity));
                    rs.first();
                    lsmIndexKey = (int) rs.getLong("index_key");
                    riIndexKey = (int) rs.getLong("remote_interlocking_index");
                    
                    dataLogger.sendToDataLogger(String.format ("%s%s%s [Valid LineSideModule: %s] %s",
                        Colour.GREEN.getColour(), getOK(), Colour.BLUE.getColour(),lsmIdentity, Colour.RESET.getColour()), 
                        true, true);
                    
                } catch (SQLException ex) {
                    
                    throw new InvalidCommandLineArgument("The LineSideModule Identity could not be validated with the Remote DataBase");
                    
                }
                
            } else { 
                
                throw new InvalidCommandLineArgument("Invalid module identity passed on the Command Line");
                
            }
            
        } else {
            
            throw new InvalidCommandLineArgument("The Module Identity was not passed on the Command Line");
            
        }
        
    }
    
    /**
     * This Method obtains the Remote Interlocking Details from the Remote DataBase.
     * 
     * This method requires that a LineSide Module identity has been established and validated before being called.
     */
    protected static void obtainRemoteInterlockingDetails() {
    
        System.out.print("Connected to remote DB - looking for Remote Interlocking details...");
        
        try {
            
            rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Remote_Interlocking WHERE index_key=%d;", riIndexKey));
            rs.first();
            riHost = rs.getString("ip_address");
            riPort = rs.getString("port_number");
            riIdentity = rs.getString("Identity");
            System.out.print(String.format ("%s%s%s ", 
                Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()));
            System.out.println(String.format("%s[%s@%s:%s]%s", 
                Colour.BLUE.getColour(), riIdentity, riHost, riPort, Colour.RESET.getColour()));
           
        } catch (SQLException ex) {
            
            System.out.println(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()));
            ExitCommandLine( String.format ("%sERROR: Cannot obtain Remote Interlocking details from the database.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()));
            
        }
    }
    
    /**
     * This method obtains the DataLogger details from the remote DB.
     * @throws customexceptions.DataLoggerException
     */
    protected static void obtainDataLoggerConnectionDetails() throws DataLoggerException {
    
        dataLogger.sendToDataLogger ("Connected to remote DB - looking for DataLogger details...", true, false);
        
        try {
            
            rs = MySqlConnect.getDbCon().query("SELECT * FROM `Data_Logger`;");
            rs.first();
            dlHost = rs.getString("ip_address");
            dlPort = rs.getString("port_number");
            
            dataLogger.sendToDataLogger(String.format ("%s%s%s [%s:%s] %s",
                Colour.GREEN.getColour(), getOK(), Colour.BLUE.getColour(), dlHost, dlPort, Colour.RESET.getColour()), 
                true, true);
            
        } catch (SQLException ex) {
            
            throw new DataLoggerException("Cannot obtain DataLogger Details from the Remote DB");
            
        }
    }
    
    /**
     * This method attempts a connection to the DataLogger.
     * 
     * This method requires that the DataLogger connection details have been obtained from the remote DB before calling this method.
     * This method also requires that the LineSide Module identity has been validated against the remote DB.
     * @throws customexceptions.DataLoggerException
     */
    protected static void attemptDataLoggerConnection() throws DataLoggerException {
        
        dataLogger.DataLoggerClientConnect(dlHost, Integer.parseInt(dlPort), lsmIdentity);
        
    }
    
    /**
     * This method builds the Points Objects.
     * 
     * This method also requires that the LineSide Module identity has been validated against the remote DB.
     */
    protected static void buildPoints() {
    
        dataLogger.sendToDataLogger("Connected to remote DB - looking for Points assigned to this LineSide Module...",true,false);
        try {
            
            rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM `Points` WHERE `parentLineSideModule` = %d;", lsmIndexKey));
            int recordsReturned = 0;
            
            while (rs.next()) {
                POINTS_ARRAY.add(new Points(rs.getString("Identity")));
                recordsReturned ++;
            }
            if (recordsReturned > 0) {
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();
                dataLogger.sendToDataLogger(String.format ("%s%-8s%-10s%-8s%s",
                    Colour.BLUE.getColour(), "Points",  "Position", "Detected", Colour.RESET.getColour()),
                    true, true);
                dataLogger.sendToDataLogger(String.format ("%s--------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                for (int i = 0; i < POINTS_ARRAY.size(); i++) {
                    dataLogger.sendToDataLogger(String.format("%s%-8s%-10s%-8s%s", 
                        Colour.BLUE.getColour(), POINTS_ARRAY.get(i).getIdentity(), POINTS_ARRAY.get(i).getPointsPosition().toString(), 
                        (POINTS_ARRAY.get(i).getDetectionStatus()) ? Colour.GREEN.getColour() + getOK() + Colour.RESET.getColour() : getFailed(), Colour.RESET.getColour()),
                        true, true);
                }
                System.out.println();
            } else {
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                    true, true);
                ExitCommandLine(String.format ("%sERROR: Cannot obtain Points details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
        } catch (SQLException ex) {
            dataLogger.sendToDataLogger(String.format ("%s%s%s",
                Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                true, true);
            ExitCommandLine(String.format ("%sERROR: Cannot obtain Points details from the database.%s",
                Colour.RED.getColour(), Colour.RESET.getColour()));
        }
    }
}

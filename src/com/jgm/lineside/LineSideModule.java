package com.jgm.lineside;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class provides the Line Side Module Functionality.
 * @author Jonathan Moss
 * @version 1.0 - August 2016
 */
public class LineSideModule {
    
    // LineSide Module class variables.
    private static String lsmIdentity = null; // Default module identity, a String representation
    private static int lsmIndexKey = 9999;
    
    // Remote Interlocking classvariables.
    private static int riIndexKey = 9999;
    private static String riHost; // The IP address of the Remote Interlocking server.
    private static String riPort; // The port number of the Remote Interlocking Server.
    private static String riIdentity;
    
    // Define arrays to receive and create the points objects.
    private static final ArrayList <Points> POINTS_ARRAY = new ArrayList <>();
  
    // Data-Logger Server Connection details
    private static String dlHost; // The IP address of the Data-Logger server.
    private static String dlPort; // The port number of the DL Server.

    // DataBase Objects
    private static ResultSet rs;
    
    // Define arrays to receive and create the controlled signals objects.
    private static final ArrayList <Controlled_Signal> CONTROLLED_SIGNAL_ARRAY = new ArrayList<>();
    
    // Define arrays to receive and create the controlled signals objects.
    private static final ArrayList <Automatic_Signal> NON_CONTROLLED_SIGNAL_ARRAY = new ArrayList<>();
    
    // Define arrays to receive and create the Train Detection objects.
    private static final ArrayList <TrainDetection> TRAIN_DETECTION_ARRAY = new ArrayList<>();
    
    protected static DataLoggerClient dataLogger;
    protected static final String NEW_LINE = System.lineSeparator();
    
    private static String OperatingSystem = System.getProperty("os.name");
 
    public static String getOK() {
        if (OperatingSystem.contains("Windows")) {
            return "OK";
        } else {
            return "[\u2713]";
        }
    }
    
    public static String getFailed() {
        if (OperatingSystem.contains("Windows")) {
            return "FAILED";
        } else {
            return "[\u2717]";
        }
    }
    
    public static void main(String[] args) throws IOException {
       
        System.out.println("Line Side Module v1.0 - Running startup script...");
        System.out.println("-------------------------------------------------\n");
        
        // 1) Obtain Lineside Module Identity from the command line.
        if (args.length > 0) { // Make sure at least 1 argument was passed.
            if (args[0].length() == 5) { // We are expecting a String of 5 characters only.
                lsmIdentity = args[0]; // Set this Lineside Module Identity.
            } else { // Invalid Lineside Module Identity.
                LineSideModule.ExitCommandLine(String.format("%sERR: Argument [1] expects a String object representing the LineSide Module Identity.%s",Colour.RED.getColour(), Colour.RED.getColour()));
            }
            
        // 2) Connect to the Database and obtain a few details about the LinesideModule based on the argument passed in 1.
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Lineside_Module WHERE identity = '%s';",lsmIdentity));
                rs.first();
                lsmIndexKey = (int) rs.getLong("index_key");
                riIndexKey = (int) rs.getLong("remote_interlocking_index");
                System.out.println(String.format ("Connected to remote DB - looking for Line Side Module details...%s%s%s", 
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("Connecting to remote DB - looking for Line Side Module details...%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()));
                LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain LineSide Module details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
        // 3) Obtain the Remote Interlocking Connection Details.
            System.out.print("Connected to remote DB - looking for Remote Interlocking details...");
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Remote_Interlocking WHERE index_key=%d;", riIndexKey));
                rs.first();
                riHost = rs.getString("ip_address");
                riPort = rs.getString("port_number");
                riIdentity = rs.getString("Identity");
                System.out.print(String.format ("%s%s%s ", 
                        Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()));
                System.out.println(String.format("%s[%s@%s:%s]%s", 
                        Colour.BLUE.getColour(), riIdentity, riHost, riPort, Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()));
                LineSideModule.ExitCommandLine( String.format ("%sERR: Cannot obtain Remote Interlocking details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
        // 4) Obtain the DataLogger Module Connection Details.
            System.out.print("Connected to remote DB - looking for Data Logger details...");
            try {
                rs = MySqlConnect.getDbCon().query("SELECT * FROM Data_Logger;");
                rs.first();
                dlHost = rs.getString("ip_address");
                dlPort = rs.getString("port_number");
                System.out.print(String.format ("%s%s%s ", 
                        Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()));
                System.out.println(String.format("%s[%s:%s]%s",
                        Colour.BLUE.getColour(), dlHost, dlPort, Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()));
                LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Data Logger details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            DataLogger:
        // 5) Open a connection to the Data Logger.
            try {
                LineSideModule.dataLogger = new DataLoggerClient(LineSideModule.dlHost, Integer.parseInt(LineSideModule.dlPort), LineSideModule.lsmIdentity);
                dataLogger.setName("DataLogger-Thread");
                dataLogger.start();
                Thread.sleep(2000);
            } catch (IOException | InterruptedException ex) {}

        
        // 6) Build the Points.
            LineSideModule.dataLogger.sendToDataLogger("Connected to remote DB - looking for Points assigned to this Line Side Module...",true,false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Points WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    POINTS_ARRAY.add(new Points(rs.getString("Identity")));
                    recordsReturned ++;
                }
                if (recordsReturned > 0) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()),
                        true,true);
                    System.out.println();
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%sPoints\tPosition\tDetected%s",
                        Colour.BLUE.getColour(), Colour.RESET.getColour()),
                        true, true);
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s------------------------------------%s",
                        Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                        true, true);
                    for (int i = 0; i < POINTS_ARRAY.size(); i++) {
                        LineSideModule.dataLogger.sendToDataLogger(String.format("%s%s\t%s\t\t%s%s", 
                            Colour.BLUE.getColour(), POINTS_ARRAY.get(i).getIdentity(), POINTS_ARRAY.get(i).getPointsPosition().toString(), POINTS_ARRAY.get(i).getDetectionStatus().toString(), Colour.RESET.getColour()),
                            true, true);
                    }
                    System.out.println();
                } else {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Points details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
            } catch (SQLException ex) {
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                    true, true);
                LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Points details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
        // 7) Build the Controlled Signals.
            LineSideModule.dataLogger.sendToDataLogger("Connected to remote DB - looking for Controlled Signals assigned to this Line Side Module...", true, false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Controlled_Signals WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    try {
                        CONTROLLED_SIGNAL_ARRAY.add(new Controlled_Signal(rs.getString("prefix"), rs.getString("identity"), Controlled_Signal_Type.valueOf(rs.getString("type"))));
                        recordsReturned ++;
                    } catch (Exception ex) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%sControlled Signal\tType\t\t   Current Aspect%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour())
                    , true, true);
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s----------------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                for (int i = 0; i < CONTROLLED_SIGNAL_ARRAY.size(); i++) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format("%s%s\t\t\t%s\t\t%s%s", 
                        Colour.BLUE.getColour(), CONTROLLED_SIGNAL_ARRAY.get(i).getFullSignalIdentity(), CONTROLLED_SIGNAL_ARRAY.get(i).getSignalType().toString(), CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString(), Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                    true, true);
                LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Controlled Signal details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
            
        // 8) Build the non-controlled signals.
            LineSideModule.dataLogger.sendToDataLogger("Connected to remote DB - looking for non-controlled Signals assigned to this Line Side Module...", true, false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Non_Controlled_Signals WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    try {
                        NON_CONTROLLED_SIGNAL_ARRAY.add(new Automatic_Signal(rs.getString("prefix"), rs.getString("identity"), 
                            Automatic_Signal_Type.valueOf(rs.getString("type")), rs.getString("line"), rs.getString("read_direction"), 
                            Function.valueOf(rs.getString("function")), rs.getString("applicable_Signal")));
                        recordsReturned ++;
                    } catch (Exception ex) {
                        LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                            Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                            true, true);
                        LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Non-Controlled Signal details from the database.%s",
                            Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Non-Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();

                LineSideModule.dataLogger.sendToDataLogger(String.format ("%sNon-Controlled Signal\tType\t\t\tCurrent Aspect\tFunction%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s---------------------------------------------------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()),
                    true, true);
                for (int i = 0; i < NON_CONTROLLED_SIGNAL_ARRAY.size(); i++) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format("%s%s\t\t\t%s\t\t%s\t\t%s%s", 
                        Colour.BLUE.getColour(), NON_CONTROLLED_SIGNAL_ARRAY.get(i).getSignalIdentity(),NON_CONTROLLED_SIGNAL_ARRAY.get(i).getType(), 
                        NON_CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect(), NON_CONTROLLED_SIGNAL_ARRAY.get(i).getFunction(), Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                    true, true);
                LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Non-Controlled Signal details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
        // 9) Build the Train Detection Sections.
            LineSideModule.dataLogger.sendToDataLogger("Connected to remote DB - looking for Train Detection Sections assigned to this Line Side Module...", true, false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Train_Detection WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    try {
                        TRAIN_DETECTION_ARRAY.add(new TrainDetection(rs.getString("identity"), TD_Type.valueOf(rs.getString("type"))));
                        recordsReturned ++;
                    } catch (Exception ex) {
                        LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                            Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                            true, true);
                        LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Train Detection details from the database.%s",
                            Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                        true, true);
                     LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Train Detection details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), LineSideModule.getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%sTrain Detection Section\tType\t\t\tStatus%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s----------------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                for (int i = 0; i < TRAIN_DETECTION_ARRAY.size(); i++) {
                    LineSideModule.dataLogger.sendToDataLogger(String.format("%s%s\t\t\t%s\t\t%s%s", 
                        Colour.BLUE.getColour(), TRAIN_DETECTION_ARRAY.get(i).getIdentity(), 
                        TRAIN_DETECTION_ARRAY.get(i).getType().toString(), TRAIN_DETECTION_ARRAY.get(i).getDetectionStatus(), Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                    true, true);
                 LineSideModule.ExitCommandLine(String.format ("%sERR: Cannot obtain Train Detection details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
        
        // 10) Open a connection to the Remote Interlocking.
            LineSideModule.dataLogger.sendToDataLogger("Attempt a connection with the Remote Interlocking...", true, false);
            LineSideModule.dataLogger.sendToDataLogger(String.format ("%s%s%s",
                Colour.RED.getColour(), LineSideModule.getFailed(), Colour.RESET.getColour()), 
                true, true);
            System.out.println();
            
        // 11) Wait for State Changes or Messages From the Remote Intelocking.
        //TODO

       } else {
           
           LineSideModule.ExitCommandLine("ERR: Incorrect number of command line arguments.");
           
       }
       
       //A few examples...
//       try {
//           
//           POINTS_ARRAY.get(Points.returnPointIndex("940")).movePointsUnderPower(PointsPosition.REVERSE);
//           System.out.println(CONTROLLED_SIGNAL_ARRAY.get(Controlled_Signal.returnControlledSignalIndex("CE175")).getCurrentAspect());
//           System.out.println(TRAIN_DETECTION_ARRAY.get(TrainDetection.returnDetectionIndex("T92")).getDetectionStatus());
//            
//       } catch (NullPointerException npE) {
//           
//           // Points with the specified identity cannot be found.
//           
//       }
       
    }
    
    /**
     * This method is called when there is a problem with the command line arguments passed.
     * It displays a message, and exits the programme.
     * @param message A <code>String</code> detailing why there is an issue.
     */
    private static void ExitCommandLine(String message) {
        
        String msg;
        msg = String.format("Line Side Module is exiting :: '%s'", message);
        System.out.println(msg);
        System.exit(0);
        
    }
    
    private static void testMessage(Points obj) {
        
        String msg;
        msg = String.format("%s\t%s\t%s", obj.getIdentity(), obj.getPointsPosition().toString(), obj.getDetectionStatus().toString().toUpperCase());
        System.out.println(msg);
        
    }
    
}

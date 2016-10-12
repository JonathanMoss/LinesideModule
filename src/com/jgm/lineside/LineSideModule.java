package com.jgm.lineside;

import com.jgm.lineside.database.MySqlConnect;
import com.jgm.lineside.traindetection.TrainDetection;
import com.jgm.lineside.traindetection.TD_Type;
import com.jgm.lineside.datalogger.Colour;
import com.jgm.lineside.datalogger.DataLoggerClient;
import com.jgm.lineside.interlocking.RemoteInterlockingClient;
import com.jgm.lineside.points.Points;
import com.jgm.lineside.signals.Automatic_Signal_Type;
import com.jgm.lineside.signals.Automatic_Signal;
import com.jgm.lineside.signals.Function;
import com.jgm.lineside.signals.Controlled_Signal;
import com.jgm.lineside.signals.Controlled_Signal_Type;
import com.jgm.lineside.signals.Aspects;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static Boolean setup = true;
    
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
    
    // Some general declarations.
    public static DataLoggerClient dataLogger;
    public static RemoteInterlockingClient remoteInterlocking;
    public static final String NEW_LINE = System.lineSeparator();
    private static String OperatingSystem = System.getProperty("os.name");
 
    public static ArrayList getPointsArray () {
        return POINTS_ARRAY;
    }
    
    public static String getLineSideModuleIdentity() {
        return lsmIdentity;
    }
    
    /**
     * This method returns a string representing 'OK' for display on the command line.
     * This method determines an appropriate indication based on the capabilities of the console.
     * 
     * @return A <code>String</code> representing 'OK' or a check mark.
     */
    public static String getOK() {
        if (OperatingSystem.contains("Windows")) {
            return "OK";
        } else {
            return "[\u2713]";
        }
    }
    
     /**
     * This method returns a string representing 'FAILED' for display on the command line.
     * This method determines an appropriate indication based on the capabilities of the console.
     * 
     * @return A <code>String</code> representing 'FAILED' or a Cross.
     */
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
                ExitCommandLine(String.format("%sERROR: Argument [1] expects a String object representing the LineSide Module Identity.%s",Colour.RED.getColour(), Colour.RED.getColour()));
            }
            
        // 2) Connect to the Database and obtain a few details about the LinesideModule based on the argument passed in 1.
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Lineside_Module WHERE identity = '%s';",lsmIdentity));
                rs.first();
                lsmIndexKey = (int) rs.getLong("index_key");
                riIndexKey = (int) rs.getLong("remote_interlocking_index");
                System.out.println(String.format ("Connected to remote DB - looking for Line Side Module details...%s%s%s", 
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("Connecting to remote DB - looking for Line Side Module details...%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()));
                ExitCommandLine(String.format ("%sERROR: Cannot obtain LineSide Module details from the database.%s",
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
                        Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()));
                System.out.println(String.format("%s[%s@%s:%s]%s", 
                        Colour.BLUE.getColour(), riIdentity, riHost, riPort, Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()));
                ExitCommandLine( String.format ("%sERROR: Cannot obtain Remote Interlocking details from the database.%s",
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
                        Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()));
                System.out.println(String.format("%s[%s:%s]%s",
                        Colour.BLUE.getColour(), dlHost, dlPort, Colour.RESET.getColour()));
            } catch (SQLException ex) {
                System.out.println(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()));
                ExitCommandLine(String.format ("%sERROR: Cannot obtain Data Logger details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
            }

        // 5) Open a connection to the Data Logger.
            try {
                dataLogger = new DataLoggerClient(dlHost, Integer.parseInt(dlPort), lsmIdentity);
                dataLogger.setName("DataLogger-Thread");
                dataLogger.start();
                Thread.sleep(2000);
            } catch (IOException | InterruptedException ex) {}

        // 6) Build the Points.
            dataLogger.sendToDataLogger("Connected to remote DB - looking for Points assigned to this Line Side Module...",true,false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Points WHERE parentLinesideModule = %d;", lsmIndexKey));
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
            
        // 7) Build the Controlled Signals.
            dataLogger.sendToDataLogger("Connected to remote DB - looking for Controlled Signals assigned to this Line Side Module...", true, false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Controlled_Signals WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    try {
                        CONTROLLED_SIGNAL_ARRAY.add(new Controlled_Signal(rs.getString("prefix"), rs.getString("identity"), Controlled_Signal_Type.valueOf(rs.getString("type"))));
                        recordsReturned ++;
                    } catch (Exception ex) {
                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    ExitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    ExitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();
                dataLogger.sendToDataLogger(String.format ("%s%-19s%-19s%-10s%s",
                    Colour.BLUE.getColour(), "Controlled Signal", "Type", "Current Aspect", Colour.RESET.getColour())
                    , true, true);
                dataLogger.sendToDataLogger(String.format ("%s----------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                for (int i = 0; i < CONTROLLED_SIGNAL_ARRAY.size(); i++) {
                    dataLogger.sendToDataLogger(String.format("%s%-19s%-19s%-10s%s", 
                        Colour.BLUE.getColour(), CONTROLLED_SIGNAL_ARRAY.get(i).getFullSignalIdentity(), CONTROLLED_SIGNAL_ARRAY.get(i).getSignalType().toString(), 
                        (CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect() == Aspects.RED) ? Colour.RED.getColour() + CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.RESET.getColour() : CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString(), 
                        Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                    true, true);
                ExitCommandLine(String.format ("%sERROR: Cannot obtain Controlled Signal details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
        // 8) Build the non-controlled signals.
            dataLogger.sendToDataLogger("Connected to remote DB - looking for non-controlled Signals assigned to this Line Side Module...", true, false);
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
                        dataLogger.sendToDataLogger(String.format ("%s%s%s",
                            Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                            true, true);
                        ExitCommandLine(String.format ("%sERROR: Cannot obtain Non-Controlled Signal details from the database.%s",
                            Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                        true, true);
                    ExitCommandLine(String.format ("%sERROR: Cannot obtain Non-Controlled Signal details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();

                dataLogger.sendToDataLogger(String.format ("%s%-22s%-18s%-16s%s%s",
                    Colour.BLUE.getColour(), "Non-Controlled Signal", "Type", "Current Aspect", "Function", Colour.RESET.getColour()), 
                    true, true);
                dataLogger.sendToDataLogger(String.format ("%s------------------------------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()),
                    true, true);
                for (int i = 0; i < NON_CONTROLLED_SIGNAL_ARRAY.size(); i++) {
                    dataLogger.sendToDataLogger(String.format("%s%-22s%-18s%-26s%s%s", 
                        Colour.BLUE.getColour(), NON_CONTROLLED_SIGNAL_ARRAY.get(i).getSignalIdentity(),NON_CONTROLLED_SIGNAL_ARRAY.get(i).getType(), 
                        (NON_CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString().contains("YELLOW") || NON_CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString().contains("WARNING"))? Colour.YELLOW.getColour() + NON_CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.BLUE.getColour() : Colour.RED.getColour() + NON_CONTROLLED_SIGNAL_ARRAY.get(i).getCurrentAspect().toString() + Colour.BLUE.getColour(), 
                        NON_CONTROLLED_SIGNAL_ARRAY.get(i).getFunction(), Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                    true, true);
                ExitCommandLine(String.format ("%sERROR: Cannot obtain Non-Controlled Signal details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
            
        // 9) Build the Train Detection Sections.
            dataLogger.sendToDataLogger("Connected to remote DB - looking for Train Detection Sections assigned to this Line Side Module...", true, false);
            try {
                rs = MySqlConnect.getDbCon().query(String.format("SELECT * FROM Train_Detection WHERE parentLinesideModule = %d;", lsmIndexKey));
                int recordsReturned = 0;
                while (rs.next()) {
                    try {
                        TRAIN_DETECTION_ARRAY.add(new TrainDetection(rs.getString("identity"), TD_Type.valueOf(rs.getString("type"))));
                        recordsReturned ++;
                    } catch (Exception ex) {
                        dataLogger.sendToDataLogger(String.format ("%s%s%s",
                            Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                            true, true);
                        ExitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
                            Colour.RED.getColour(), Colour.RESET.getColour()));
                    }
                }
                if (recordsReturned == 0) {
                    dataLogger.sendToDataLogger(String.format ("%s%s%s",
                        Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                        true, true);
                     ExitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
                        Colour.RED.getColour(), Colour.RESET.getColour()));
                }
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.GREEN.getColour(), getOK(), Colour.RESET.getColour()),
                    true,true);
                System.out.println();
                dataLogger.sendToDataLogger(String.format ("%s%-26s%-18s%s%s",
                    Colour.BLUE.getColour(), "Train Detection Section", "Type", "Status", Colour.RESET.getColour()), 
                    true, true);
                dataLogger.sendToDataLogger(String.format ("%s--------------------------------------------------%s",
                    Colour.BLUE.getColour(), Colour.RESET.getColour()), 
                    true, true);
                for (int i = 0; i < TRAIN_DETECTION_ARRAY.size(); i++) {
                    dataLogger.sendToDataLogger(String.format("%s%-26s%-18s%-10s%s", 
                        Colour.BLUE.getColour(), TRAIN_DETECTION_ARRAY.get(i).getIdentity(), TRAIN_DETECTION_ARRAY.get(i).getType().toString(),
                        (TRAIN_DETECTION_ARRAY.get(i).getDetectionStatus().toString().contains("CLEAR"))? Colour.GREEN.getColour() + "CLEAR" + Colour.RESET.getColour() : Colour.RED.getColour() + "OCCUPIED" + Colour.RESET.getColour(), 
                        Colour.RESET.getColour()), 
                        true, true);
                }
                System.out.println();
            } catch (SQLException ex) {
                dataLogger.sendToDataLogger(String.format ("%s%s%s",
                    Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                    true, true);
                 ExitCommandLine(String.format ("%sERROR: Cannot obtain Train Detection details from the database.%s",
                    Colour.RED.getColour(), Colour.RESET.getColour()));
            }
        
        // 10) Open a connection to the Remote Interlocking.
            dataLogger.sendToDataLogger("Attempt a connection with the Remote Interlocking...", true, false);
            //dataLogger.sendToDataLogger(String.format ("%s%s%s",
                //Colour.RED.getColour(), getFailed(), Colour.RESET.getColour()), 
                //true, true);
            remoteInterlocking = new RemoteInterlockingClient(riHost, Integer.parseInt(riPort), riIdentity);
            remoteInterlocking.setName("RemoteInterlockingClient");
            remoteInterlocking.start();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {

            }
            
            if (setup) {
                String msgBody = String.format ("%s|HAND_SHAKE|NULL", lsmIdentity);
                String msgEnd = "|END_MESSAGE";
                remoteInterlocking.outgoing.sendMessageToRemoteInterlocking(String.format ("%s|%s%s",
                    msgBody, msgBody.hashCode(), msgEnd));
                
                
            }
            
            while (setup) {
                
            }
            
            //System.out.println();
            
        // 11) Wait for State Changes or Messages From the Remote Intelocking.
        //TODO

       } else {
           
           ExitCommandLine("ERROR: Incorrect number of command line arguments.");
           
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
     * It displays a message, and exits the program.
     * @param message A <code>String</code> detailing why there is an issue.
     */
    public static void ExitCommandLine(String message) {
        
        String msg;
        msg = String.format("Line Side Module cannot continue [%s]", message);
        if (dataLogger != null) {
            try {
                dataLogger.sendToDataLogger(message, Boolean.FALSE, Boolean.TRUE);
            } catch (IOException ex) {}
        }
        System.out.println(msg);
        System.exit(0);
    }
}

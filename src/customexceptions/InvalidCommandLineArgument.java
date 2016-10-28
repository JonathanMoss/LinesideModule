package customexceptions;
/**
 * The class InvalidCommandLineArgument is a form of Exception that indicates conditions that a reasonable application might want to catch.
 * <p>
 * This Custom Exception is used to catch the condition where the arguments passed on the command line are invalid.
 * @author Jonathan Moss
 * @version v1.0 October 2016
 */
public class InvalidCommandLineArgument extends Exception{

    /**
     * Constructs a new exception with the specified detail message.
     * @param message <code>String</code>  the detail message (which is saved for later retrieval by the getMessage() method).
     */
    public InvalidCommandLineArgument (String message) {
        super(message);
    }
    
}

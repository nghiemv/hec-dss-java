package mil.army.usace.hec.dss;

/**
 * Checked exception for DSS operations.
 * Thrown when a DSS file operation fails (open, read, catalog).
 */
public class DssException extends Exception {
    public DssException(String message) {
        super(message);
    }

    public DssException(String message, Throwable cause) {
        super(message, cause);
    }
}

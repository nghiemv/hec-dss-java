package mil.army.usace.hec.dss;

/**
 * Runtime exception for DSS operations.
 * The message describes exactly what went wrong — no need to parse or inspect fields.
 *
 * <pre>{@code
 * // Happy path — no try-catch needed
 * var ts = HecDss.readTimeSeries(file, pathname);
 *
 * // When you want to handle errors
 * try {
 *     var ts = HecDss.readTimeSeries(file, pathname);
 * } catch (DssException e) {
 *     log.error("DSS operation failed", e);
 * }
 * }</pre>
 */
public class DssException extends RuntimeException {

    public DssException(String message) {
        super(message);
    }

    public DssException(String message, Throwable cause) {
        super(message, cause);
    }
}

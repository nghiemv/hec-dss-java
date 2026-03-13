package mil.army.usace.hec.dss;

import java.time.Instant;

/**
 * Represents a time window with a fixed start and end time.
 */
public record DssTimeWindow(Instant start, Instant end) {
    /**
     * Creates a time window with the specified start and end times.
     */
    public DssTimeWindow {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end times must be specified");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

}

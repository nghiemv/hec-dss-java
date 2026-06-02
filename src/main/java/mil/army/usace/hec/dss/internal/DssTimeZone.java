package mil.army.usace.hec.dss.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Centralizes timezone handling for DSS time series.
 *
 * <p>DSS stores calendar date+time with no timezone semantics. The timezone
 * string is a label that tells us how to interpret the stored times.
 * This class provides the single source of truth for converting between
 * DSS native calendar times and Java UTC Instants.
 */
public final class DssTimeZone {
    private DssTimeZone() {}

    /**
     * Parses a DSS timezone string into a ZoneId, or null if empty/invalid.
     */
    public static ZoneId parse(String dssTimezoneString) {
        if (dssTimezoneString == null || dssTimezoneString.isEmpty()) {
            return null;
        }
        try {
            return ZoneId.of(dssTimezoneString);
        } catch (Exception ignored) {
            // Try uppercase — DSS files may have "utc" or "est"
            try {
                return ZoneId.of(dssTimezoneString.toUpperCase(java.util.Locale.ROOT));
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

    /**
     * Returns the zone to use for time conversion: the stored zone if available, otherwise UTC.
     */
    public static ZoneId zoneOrUtc(ZoneId timeZone) {
        return timeZone != null ? timeZone : ZoneOffset.UTC;
    }

    /**
     * Converts DSS native epoch seconds (calendar time treated as UTC) to a proper
     * UTC Instant by reinterpreting through the given timezone.
     *
     * @param nativeEpochSeconds epoch seconds computed from DSS Julian+seconds (as if UTC)
     * @param timeZone           the timezone the data was stored in (null = UTC)
     */
    public static Instant nativeToInstant(long nativeEpochSeconds, ZoneId timeZone) {
        ZoneId zone = zoneOrUtc(timeZone);
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(nativeEpochSeconds, 0, ZoneOffset.UTC);
        return ldt.atZone(zone).toInstant();
    }

    /**
     * Formats the DSS timezone string for native storage.
     */
    public static String toDssString(ZoneId timeZone) {
        return timeZone != null ? timeZone.getId() : "";
    }
}

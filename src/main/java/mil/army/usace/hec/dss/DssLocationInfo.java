package mil.army.usace.hec.dss;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Location metadata for a DSS record.
 *
 * @param latitude    geographic latitude in degrees
 * @param longitude   geographic longitude in degrees
 * @param elevation   elevation in meters
 * @param crs         coordinate reference system (must not be null)
 * @param timeZone    time zone (may be null if unknown)
 * @param description supplemental description text (null is coerced to "")
 */
public record DssLocationInfo(
        double latitude, double longitude, double elevation,
        DssCrs crs,
        ZoneId timeZone, String description
) {
    public DssLocationInfo {
        Objects.requireNonNull(crs, "crs must not be null");
        description = Objects.requireNonNullElse(description, "");
    }

    /**
     * Creates location info with WGS84 geographic coordinates and no description.
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, ZoneId timeZone) {
        return new DssLocationInfo(latitude, longitude, elevation, DssCrs.WGS84, timeZone, "");
    }
}

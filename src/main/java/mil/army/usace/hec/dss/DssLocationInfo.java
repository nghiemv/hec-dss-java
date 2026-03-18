package mil.army.usace.hec.dss;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Location metadata for a DSS record.
 *
 * @param latitude    geographic latitude in degrees
 * @param longitude   geographic longitude in degrees
 * @param elevation   elevation in meters
 * @param crs         coordinate reference system
 * @param timeZone    time zone (null if unknown)
 * @param description supplemental description text
 */
public record DssLocationInfo(
        double latitude, double longitude, double elevation,
        DssCrs crs,
        ZoneId timeZone, String description
) {
    public DssLocationInfo {
        Objects.requireNonNull(crs);
        Objects.requireNonNull(description);
    }

    /**
     * Creates location info with WGS84 geographic coordinates.
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, ZoneId timeZone) {
        return new DssLocationInfo(latitude, longitude, elevation, DssCrs.WGS84, timeZone, "");
    }
}

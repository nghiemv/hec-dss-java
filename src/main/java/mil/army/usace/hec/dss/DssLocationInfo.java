package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Location metadata for a DSS record.
 */
public record DssLocationInfo(
        double x, double y, double z,
        Crs crs,
        String timeZoneName, String supplemental
) {
    public DssLocationInfo {
        Objects.requireNonNull(crs);
        Objects.requireNonNull(timeZoneName);
        Objects.requireNonNull(supplemental);
    }

    /**
     * Creates location info with WGS84 geographic coordinates.
     *
     * @param latitude  WGS84 latitude in degrees
     * @param longitude WGS84 longitude in degrees
     * @param elevation elevation in meters
     * @param timeZone  time zone name (e.g. "UTC")
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, String timeZone) {
        return new DssLocationInfo(longitude, latitude, elevation, Crs.WGS84, timeZone, "");
    }
}

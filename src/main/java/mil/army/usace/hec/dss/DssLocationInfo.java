package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Location metadata for a DSS record.
 *
 * <p>The {@code crs} field holds a CRS identifier such as {@code "EPSG:4326"}
 * (WGS84 geographic) or {@code "EPSG:4269"} (NAD83 geographic).
 * The library maps these to the native DSS coordinate codes internally.
 *
 * <p>Supported CRS identifiers:
 * <ul>
 *   <li>{@code "EPSG:4326"} — WGS84 geographic (longitude/latitude in degrees)
 *   <li>{@code "EPSG:4269"} — NAD83 geographic (longitude/latitude in degrees)
 *   <li>{@code "EPSG:326{zone}"} — UTM WGS84 northern hemisphere (e.g. "EPSG:32610" for zone 10)
 *   <li>{@code "EPSG:269{zone}"} — UTM NAD83 (e.g. "EPSG:26915" for zone 15)
 *   <li>{@code ""} — no CRS specified
 * </ul>
 */
public record DssLocationInfo(
        double x, double y, double z,
        String crs,
        String timeZoneName, String supplemental
) {
    public DssLocationInfo {
        Objects.requireNonNull(crs);
        Objects.requireNonNull(timeZoneName);
        Objects.requireNonNull(supplemental);
    }

    /**
     * Creates location info with WGS84 geographic coordinates (EPSG:4326).
     *
     * @param latitude  WGS84 latitude in degrees
     * @param longitude WGS84 longitude in degrees
     * @param elevation elevation in meters
     * @param timeZone  time zone name (e.g. "UTC")
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, String timeZone) {
        return new DssLocationInfo(longitude, latitude, elevation, "EPSG:4326", timeZone, "");
    }
}

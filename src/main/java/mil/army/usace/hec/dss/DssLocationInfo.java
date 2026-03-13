package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Location metadata for a DSS record.
 */
public record DssLocationInfo(
        double x, double y, double z,
        int coordinateSystem, int coordinateId,
        int horizontalUnits, int horizontalDatum,
        int verticalUnits, int verticalDatum,
        String timeZoneName, String supplemental
) {
    public DssLocationInfo {
        Objects.requireNonNull(timeZoneName);
        Objects.requireNonNull(supplemental);
    }

    /**
     * Creates location info with WGS84 geographic coordinates.
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, String timeZone) {
        return new DssLocationInfo(
                longitude, latitude, elevation,
                2, 0,  // coordinateSystem=geographic, coordinateId=0
                3, 2,  // horizontalUnits=degrees, horizontalDatum=WGS84
                1, 1,  // verticalUnits=meters, verticalDatum=NAVD88
                timeZone, ""
        );
    }
}

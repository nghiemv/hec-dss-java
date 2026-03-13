package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Location metadata for a DSS record.
 */
public record DssLocationInfo(
        double x, double y, double z,
        CoordinateSystem coordinateSystem, int coordinateId,
        LengthUnit horizontalUnits, HorizontalDatum horizontalDatum,
        LengthUnit verticalUnits, VerticalDatum verticalDatum,
        String timeZoneName, String supplemental
) {
    public DssLocationInfo {
        Objects.requireNonNull(coordinateSystem);
        Objects.requireNonNull(horizontalUnits);
        Objects.requireNonNull(horizontalDatum);
        Objects.requireNonNull(verticalUnits);
        Objects.requireNonNull(verticalDatum);
        Objects.requireNonNull(timeZoneName);
        Objects.requireNonNull(supplemental);
    }

    /**
     * Creates location info with WGS84 geographic coordinates.
     */
    public static DssLocationInfo of(double latitude, double longitude, double elevation, String timeZone) {
        return new DssLocationInfo(
                longitude, latitude, elevation,
                CoordinateSystem.GEOGRAPHIC, 0,
                LengthUnit.DEGREES, HorizontalDatum.WGS84,
                LengthUnit.METERS, VerticalDatum.NAVD88,
                timeZone, ""
        );
    }
}

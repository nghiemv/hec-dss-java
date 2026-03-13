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
}

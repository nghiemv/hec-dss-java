package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssCrs;

/**
 * Bidirectional mapping between {@link DssCrs} and the native DSS location codes
 * (coordinate system, datum, units).
 */
public final class CrsMapping {

    // Native coordinate system codes
    private static final int CS_NONE = 0;
    private static final int CS_GEOGRAPHIC = 2;

    // Native horizontal datum codes
    private static final int HD_NONE = 0;
    private static final int HD_NAD83 = 1;
    private static final int HD_WGS84 = 2;

    // Native length unit codes
    private static final int LU_NONE = 0;
    private static final int LU_DEGREES = 3;

    private CrsMapping() {}

    /**
     * Native DSS location codes packed into a single object for reader/writer use.
     */
    public record NativeCodes(
            int coordinateSystem, int coordinateId,
            int horizontalUnits, int horizontalDatum,
            int verticalUnits, int verticalDatum
    ) {}

    /**
     * Converts native DSS codes to a {@link DssCrs}.
     */
    public static DssCrs toCrs(int coordinateSystem, int coordinateId,
                             int horizontalUnits, int horizontalDatum,
                             int verticalUnits, int verticalDatum) {
        if (coordinateSystem == CS_GEOGRAPHIC && horizontalUnits == LU_DEGREES) {
            if (horizontalDatum == HD_WGS84) return DssCrs.WGS84;
            if (horizontalDatum == HD_NAD83) return DssCrs.NAD83;
        }
        return DssCrs.NONE;
    }

    /**
     * Converts a {@link DssCrs} to native DSS codes.
     */
    public static NativeCodes fromCrs(DssCrs crs) {
        return switch (crs) {
            case WGS84 -> new NativeCodes(CS_GEOGRAPHIC, 0, LU_DEGREES, HD_WGS84, LU_NONE, HD_NONE);
            case NAD83 -> new NativeCodes(CS_GEOGRAPHIC, 0, LU_DEGREES, HD_NAD83, LU_NONE, HD_NONE);
            case NONE, SHG, HRAP, ALBERS -> new NativeCodes(CS_NONE, 0, LU_NONE, HD_NONE, LU_NONE, HD_NONE);
        };
    }
}

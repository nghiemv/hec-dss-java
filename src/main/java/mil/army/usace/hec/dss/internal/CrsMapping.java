package mil.army.usace.hec.dss.internal;

/**
 * Bidirectional mapping between CRS identifier strings (e.g. "EPSG:4326")
 * and the native DSS location codes (coordinate system, datum, units, etc.).
 *
 * <p>DSS stores location metadata as a set of integer codes:
 * coordinateSystem, coordinateId, horizontalUnits, horizontalDatum,
 * verticalUnits, verticalDatum. This class converts to/from standard
 * CRS identifiers so the public API can use a single string.
 */
public final class CrsMapping {

    // Native coordinate system codes
    static final int CS_NONE = 0;
    static final int CS_UTM = 1;
    static final int CS_GEOGRAPHIC = 2;
    static final int CS_STATE_PLANE = 3;

    // Native horizontal datum codes
    static final int HD_NONE = 0;
    static final int HD_NAD83 = 1;
    static final int HD_WGS84 = 2;

    // Native vertical datum codes
    static final int VD_NONE = 0;
    static final int VD_NAVD88 = 1;
    static final int VD_NGVD29 = 2;

    // Native length unit codes
    static final int LU_NONE = 0;
    static final int LU_FEET = 1;
    static final int LU_METERS = 2;
    static final int LU_DEGREES = 3;

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
     * Converts native DSS codes to a CRS identifier string.
     *
     * <p>Well-known combinations map to EPSG codes:
     * <ul>
     *   <li>Geographic WGS84 → "EPSG:4326"
     *   <li>Geographic NAD83 → "EPSG:4269"
     *   <li>UTM WGS84 zone N → "EPSG:326{zone}" (northern hemisphere)
     *   <li>UTM NAD83 zone N → "EPSG:269{zone}"
     * </ul>
     *
     * <p>Unknown combinations produce an empty string.
     */
    public static String toCrs(int coordinateSystem, int coordinateId,
                               int horizontalUnits, int horizontalDatum,
                               int verticalUnits, int verticalDatum) {
        if (coordinateSystem == CS_GEOGRAPHIC && horizontalUnits == LU_DEGREES) {
            if (horizontalDatum == HD_WGS84) return "EPSG:4326";
            if (horizontalDatum == HD_NAD83) return "EPSG:4269";
        }

        if (coordinateSystem == CS_UTM && horizontalUnits == LU_METERS && coordinateId > 0) {
            if (horizontalDatum == HD_WGS84) return "EPSG:326" + twoDigitZone(coordinateId);
            if (horizontalDatum == HD_NAD83) return "EPSG:269" + twoDigitZone(coordinateId);
        }

        if (coordinateSystem == CS_NONE && horizontalDatum == HD_NONE) {
            return "";
        }

        return "";
    }

    /**
     * Converts a CRS identifier string to native DSS codes.
     *
     * <p>Supports EPSG codes for geographic and UTM coordinate systems.
     * Unrecognized strings map to all-zero (NONE) codes.
     */
    public static NativeCodes fromCrs(String crs) {
        if (crs == null || crs.isEmpty()) {
            return new NativeCodes(CS_NONE, 0, LU_NONE, HD_NONE, LU_NONE, VD_NONE);
        }

        if (!crs.startsWith("EPSG:")) {
            return new NativeCodes(CS_NONE, 0, LU_NONE, HD_NONE, LU_NONE, VD_NONE);
        }

        int code;
        try {
            code = Integer.parseInt(crs.substring(5));
        } catch (NumberFormatException e) {
            return new NativeCodes(CS_NONE, 0, LU_NONE, HD_NONE, LU_NONE, VD_NONE);
        }

        // Geographic WGS84
        if (code == 4326) {
            return new NativeCodes(CS_GEOGRAPHIC, 0, LU_DEGREES, HD_WGS84, LU_NONE, VD_NONE);
        }

        // Geographic NAD83
        if (code == 4269) {
            return new NativeCodes(CS_GEOGRAPHIC, 0, LU_DEGREES, HD_NAD83, LU_NONE, VD_NONE);
        }

        // UTM WGS84 northern hemisphere: EPSG:32601-32660
        if (code >= 32601 && code <= 32660) {
            int zone = code - 32600;
            return new NativeCodes(CS_UTM, zone, LU_METERS, HD_WGS84, LU_NONE, VD_NONE);
        }

        // UTM NAD83: EPSG:26901-26923
        if (code >= 26901 && code <= 26923) {
            int zone = code - 26900;
            return new NativeCodes(CS_UTM, zone, LU_METERS, HD_NAD83, LU_NONE, VD_NONE);
        }

        return new NativeCodes(CS_NONE, 0, LU_NONE, HD_NONE, LU_NONE, VD_NONE);
    }

    private static String twoDigitZone(int zone) {
        return zone < 10 ? "0" + zone : String.valueOf(zone);
    }
}

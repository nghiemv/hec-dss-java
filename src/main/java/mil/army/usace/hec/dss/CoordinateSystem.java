package mil.army.usace.hec.dss;

/**
 * Coordinate system used for location data.
 */
public enum CoordinateSystem {
    NONE(0),
    UTM(1),
    GEOGRAPHIC(2),
    STATE_PLANE(3);

    private final int code;

    CoordinateSystem(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static CoordinateSystem fromCode(int code) {
        for (CoordinateSystem cs : values()) {
            if (cs.code == code) return cs;
        }
        return NONE;
    }
}

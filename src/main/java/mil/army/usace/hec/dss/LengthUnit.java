package mil.army.usace.hec.dss;

/**
 * Unit of length/distance measurement.
 */
public enum LengthUnit {
    NONE(0),
    FEET(1),
    METERS(2),
    DEGREES(3);

    private final int code;

    LengthUnit(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static LengthUnit fromCode(int code) {
        for (LengthUnit u : values()) {
            if (u.code == code) return u;
        }
        return NONE;
    }
}

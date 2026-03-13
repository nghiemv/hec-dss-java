package mil.army.usace.hec.dss;

/**
 * The coordinate projection system used by a grid.
 */
public enum GridType {
    UNDEFINED(400),
    UNDEFINED_NO_TIME(401),
    HRAP(410),
    HRAP_NO_TIME(411),
    ALBERS(420),
    ALBERS_NO_TIME(421),
    SHG(430),
    SHG_NO_TIME(431);

    private final int code;

    GridType(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static GridType fromCode(int code) {
        for (GridType t : values()) {
            if (t.code == code) return t;
        }
        return UNDEFINED;
    }
}

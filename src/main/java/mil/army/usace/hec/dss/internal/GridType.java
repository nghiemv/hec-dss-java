package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.Crs;

/**
 * Native DSS grid type codes. Maps between {@link Crs} and native integer codes.
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

    public Crs toCrs() {
        return switch (this) {
            case SHG, SHG_NO_TIME -> Crs.SHG;
            case HRAP, HRAP_NO_TIME -> Crs.HRAP;
            case ALBERS, ALBERS_NO_TIME -> Crs.ALBERS;
            case UNDEFINED, UNDEFINED_NO_TIME -> Crs.NONE;
        };
    }

    public boolean isTimeStamped() {
        return code % 2 == 0;
    }

    public static GridType fromCrs(Crs crs, boolean timeStamped) {
        return switch (crs) {
            case SHG -> timeStamped ? SHG : SHG_NO_TIME;
            case HRAP -> timeStamped ? HRAP : HRAP_NO_TIME;
            case ALBERS -> timeStamped ? ALBERS : ALBERS_NO_TIME;
            default -> timeStamped ? UNDEFINED : UNDEFINED_NO_TIME;
        };
    }

    public static GridType fromCode(int code) {
        for (GridType t : values()) {
            if (t.code == code) return t;
        }
        return UNDEFINED;
    }
}

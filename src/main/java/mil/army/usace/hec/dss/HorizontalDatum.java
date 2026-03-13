package mil.army.usace.hec.dss;

/**
 * Horizontal geodetic datum.
 */
public enum HorizontalDatum {
    NONE(0),
    NAD83(1),
    WGS84(2);

    private final int code;

    HorizontalDatum(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static HorizontalDatum fromCode(int code) {
        for (HorizontalDatum d : values()) {
            if (d.code == code) return d;
        }
        return NONE;
    }
}

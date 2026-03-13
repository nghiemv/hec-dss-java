package mil.army.usace.hec.dss;

/**
 * Vertical geodetic datum.
 */
public enum VerticalDatum {
    NONE(0),
    NAVD88(1),
    NGVD29(2);

    private final int code;

    VerticalDatum(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static VerticalDatum fromCode(int code) {
        for (VerticalDatum d : values()) {
            if (d.code == code) return d;
        }
        return NONE;
    }
}

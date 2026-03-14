package mil.army.usace.hec.dss;

/**
 * What the cell values in a grid represent over time.
 */
public enum GridDataType {
    PERIOD_AVERAGE(0),
    PERIOD_CUMULATIVE(1),
    INSTANTANEOUS(2),
    INSTANTANEOUS_CUMULATIVE(3),
    FREQUENCY(4);

    private final int code;

    GridDataType(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static GridDataType fromCode(int code) {
        for (GridDataType t : values()) {
            if (t.code == code) return t;
        }
        return PERIOD_AVERAGE;
    }
}

package mil.army.usace.hec.dss;

/**
 * What the cell values in a grid represent over time.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/zStructSpatialGrid.h">
 *      zStructSpatialGrid.h — dataType enum</a>
 */
public enum GridDataType {
    PER_AVER(0),
    PER_CUM(1),
    INST_VAL(2),
    INST_CUM(3),
    FREQ(4);

    private final int code;

    GridDataType(int code) {
        this.code = code;
    }

    public int code() { return code; }

    public static GridDataType fromCode(int code) {
        for (GridDataType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown grid data type code: %d".formatted(code));
    }
}

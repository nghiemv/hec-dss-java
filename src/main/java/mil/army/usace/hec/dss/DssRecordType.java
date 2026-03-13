package mil.army.usace.hec.dss;

/**
 * The type of data stored in a DSS record.
 */
public enum DssRecordType {
    REGULAR_TIME_SERIES,
    IRREGULAR_TIME_SERIES,
    PAIRED_DATA,
    TEXT,
    GRID,
    TIN,
    LOCATION_INFO,
    ARRAY,
    UNKNOWN;

    public static DssRecordType fromNativeCode(int code) {
        if (code >= 90 && code <= 93) return ARRAY;
        if (code >= 100 && code < 110) return REGULAR_TIME_SERIES;
        if (code >= 110 && code < 200) return IRREGULAR_TIME_SERIES;
        if (code >= 200 && code < 300) return PAIRED_DATA;
        if (code >= 300 && code < 400) return TEXT;
        if (code >= 400 && code < 450) return GRID;
        if (code == 450) return TIN;
        if (code == 20) return LOCATION_INFO;
        return UNKNOWN;
    }
}

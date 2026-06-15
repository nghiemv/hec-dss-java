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
    UNKNOWN
}

package mil.army.usace.hec.dss;

/**
 * What the values in a time series represent over time.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/hecdss/hecdss.h">
 *      hecdss.h — type parameter in hec_dss_tsStoreRegular / hec_dss_tsRetrieve</a>
 */
public enum TimeSeriesDataType {
    INST_VAL("INST-VAL"),
    INST_CUM("INST-CUM"),
    PER_AVER("PER-AVER"),
    PER_CUM("PER-CUM");

    private final String dssString;

    TimeSeriesDataType(String dssString) {
        this.dssString = dssString;
    }

    /** Returns the DSS-native string representation (e.g. "INST-VAL"). */
    public String dssString() { return dssString; }

    /** Parses a DSS-native string (e.g. "INST-VAL") to the corresponding enum value. */
    public static TimeSeriesDataType fromDssString(String s) {
        for (TimeSeriesDataType t : values()) {
            if (t.dssString.equalsIgnoreCase(s)) return t;
        }
        throw new IllegalArgumentException("Unknown time series data type: '%s'".formatted(s));
    }
}

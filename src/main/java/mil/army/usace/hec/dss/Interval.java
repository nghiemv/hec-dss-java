package mil.army.usace.hec.dss;

/**
 * Time series interval — the E-part of a DSS pathname.
 *
 * <p>Regular intervals define the spacing between values.
 * Irregular intervals define the block size for storage grouping.
 *
 * <p>Use these constants when constructing pathnames for time series:
 * <pre>{@code
 * String pathname = "/BASIN/OUTLET/FLOW/01Jan2020/" + Interval.HOUR_1 + "/RUN:1/";
 * }</pre>
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/standardIntervals.h">
 *      standardIntervals.h — eParts7 array</a>
 */
public final class Interval {
    private Interval() {}

    // ---- Regular: seconds ----
    public static final String SECOND_1 = "1Second";
    public static final String SECOND_2 = "2Second";
    public static final String SECOND_3 = "3Second";
    public static final String SECOND_4 = "4Second";
    public static final String SECOND_5 = "5Second";
    public static final String SECOND_6 = "6Second";
    public static final String SECOND_10 = "10Second";
    public static final String SECOND_15 = "15Second";
    public static final String SECOND_20 = "20Second";
    public static final String SECOND_30 = "30Second";

    // ---- Regular: minutes ----
    public static final String MINUTE_1 = "1Minute";
    public static final String MINUTE_2 = "2Minute";
    public static final String MINUTE_3 = "3Minute";
    public static final String MINUTE_4 = "4Minute";
    public static final String MINUTE_5 = "5Minute";
    public static final String MINUTE_6 = "6Minute";
    public static final String MINUTE_10 = "10Minute";
    public static final String MINUTE_12 = "12Minute";
    public static final String MINUTE_15 = "15Minute";
    public static final String MINUTE_20 = "20Minute";
    public static final String MINUTE_30 = "30Minute";

    // ---- Regular: hours ----
    public static final String HOUR_1 = "1Hour";
    public static final String HOUR_2 = "2Hour";
    public static final String HOUR_3 = "3Hour";
    public static final String HOUR_4 = "4Hour";
    public static final String HOUR_6 = "6Hour";
    public static final String HOUR_8 = "8Hour";
    public static final String HOUR_12 = "12Hour";

    // ---- Regular: days and above ----
    public static final String DAY_1 = "1Day";
    public static final String WEEK_1 = "1Week";
    public static final String TRI_MONTH = "Tri-Month";
    public static final String SEMI_MONTH = "Semi-Month";
    public static final String MONTH_1 = "1Month";
    public static final String YEAR_1 = "1Year";

    // ---- Irregular ----
    public static final String IR_DAY = "IR-Day";
    public static final String IR_MONTH = "IR-Month";
    public static final String IR_YEAR = "IR-Year";
    public static final String IR_DECADE = "IR-Decade";
    public static final String IR_CENTURY = "IR-Century";
}

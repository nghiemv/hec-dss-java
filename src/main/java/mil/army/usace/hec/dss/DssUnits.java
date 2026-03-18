package mil.army.usace.hec.dss;

/**
 * Common DSS unit strings.
 *
 * <p>These are convenience constants — DSS accepts any unit string.
 * Use these for discoverability; pass any string if your unit isn't listed.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/hecdss/hecdss.h">
 *      hecdss.h — units parameter</a>
 */
public final class DssUnits {
    private DssUnits() {}

    // Flow
    public static final String CFS = "CFS";
    public static final String CMS = "CMS";

    // Length / Depth
    public static final String FEET = "FEET";
    public static final String FT = "FT";
    public static final String IN = "IN";
    public static final String INCHES = "INCHES";
    public static final String MM = "MM";
    public static final String CM = "CM";
    public static final String M = "M";

    // Volume
    public static final String AC_FT = "AC-FT";

    // Area
    public static final String AC = "AC";
    public static final String SQ_MI = "SQ MI";
    public static final String SQ_KM = "SQ KM";

    // Temperature
    public static final String DEG_F = "DEG F";
    public static final String DEG_C = "DEG C";

    // Velocity
    public static final String FT_S = "FT/S";
    public static final String M_S = "M/S";

    // Energy / Power
    public static final String KW = "KW";
    public static final String MW = "MW";
    public static final String KWH = "KWH";
    public static final String MWH = "MWH";

    // Pressure
    public static final String IN_HG = "IN-HG";
    public static final String MB = "MB";

    // Concentration
    public static final String MG_L = "MG/L";
    public static final String UG_L = "UG/L";

    // Dimensionless
    public static final String PERCENT = "%";
    public static final String UNSPECIFIED = "UNSPECIFIED";
    public static final String N_A = "N/A";
}

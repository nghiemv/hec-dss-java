package mil.army.usace.hec.dss;

/**
 * Common DSS parameter type strings for paired data axes.
 *
 * <p>These are convenience constants — DSS accepts any parameter string.
 * Use these for discoverability; pass any string if your parameter isn't listed.
 *
 * <p><b>Case note:</b> DSS stores these strings as-is with no case normalization.
 * Use case-insensitive comparison ({@link String#equalsIgnoreCase}) when comparing
 * parameter values from different sources.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/zStructPairedData.h">
 *      zStructPairedData.h — typeIndependent / typeDependent fields</a>
 */
public final class DssParameters {
    private DssParameters() {}

    public static final String FLOW = "Flow";
    public static final String STAGE = "Stage";
    public static final String ELEVATION = "Elev";
    public static final String AREA = "Area";
    public static final String STORAGE = "Storage";
    public static final String PRECIPITATION = "Precip";
    public static final String TEMPERATURE = "Temp";
    public static final String VELOCITY = "Velocity";
    public static final String DEPTH = "Depth";
    public static final String ENERGY = "Energy";
    public static final String POWER = "Power";
    public static final String CONCENTRATION = "Conc";
    public static final String PRESSURE = "Pressure";
}

package mil.army.usace.hec.dss;

/**
 * Common DSS parameter type strings for paired data axes.
 *
 * <p>These are convenience constants — DSS accepts any parameter string.
 * Use these for discoverability; pass any string if your parameter isn't listed.
 * Values read from DSS files are normalized to uppercase.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/zStructPairedData.h">
 *      zStructPairedData.h — typeIndependent / typeDependent fields</a>
 */
public final class DssParameters {
    private DssParameters() {}

    public static final String FLOW = "FLOW";
    public static final String STAGE = "STAGE";
    public static final String ELEVATION = "ELEV";
    public static final String AREA = "AREA";
    public static final String STORAGE = "STORAGE";
    public static final String PRECIPITATION = "PRECIP";
    public static final String TEMPERATURE = "TEMP";
    public static final String VELOCITY = "VELOCITY";
    public static final String DEPTH = "DEPTH";
    public static final String ENERGY = "ENERGY";
    public static final String POWER = "POWER";
    public static final String CONCENTRATION = "CONC";
    public static final String PRESSURE = "PRESSURE";
}

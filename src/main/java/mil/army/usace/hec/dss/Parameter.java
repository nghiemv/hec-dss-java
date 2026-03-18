package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * A DSS parameter type describing what a data axis represents (e.g. "Stage", "Flow").
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/zStructPairedData.h">
 *      zStructPairedData.h — typeIndependent / typeDependent fields</a>
 *
 * <p>Common parameters are provided as constants. For less common or custom
 * parameters, use {@link #of(String)}.
 *
 * <pre>{@code
 * // Use a built-in constant:
 * Parameter p = Parameter.FLOW;
 *
 * // Or create a custom one:
 * Parameter p = Parameter.of("Sediment Load");
 * }</pre>
 */
public final class Parameter {
    // ---- Common parameters ----
    public static final Parameter FLOW = new Parameter("Flow");
    public static final Parameter STAGE = new Parameter("Stage");
    public static final Parameter ELEVATION = new Parameter("Elev");
    public static final Parameter AREA = new Parameter("Area");
    public static final Parameter STORAGE = new Parameter("Storage");
    public static final Parameter PRECIPITATION = new Parameter("Precip");
    public static final Parameter TEMPERATURE = new Parameter("Temp");
    public static final Parameter VELOCITY = new Parameter("Velocity");
    public static final Parameter DEPTH = new Parameter("Depth");
    public static final Parameter ENERGY = new Parameter("Energy");
    public static final Parameter POWER = new Parameter("Power");
    public static final Parameter CONCENTRATION = new Parameter("Conc");
    public static final Parameter CONDUCTANCE = new Parameter("Conductance");
    public static final Parameter PH = new Parameter("pH");
    public static final Parameter TURBIDITY = new Parameter("Turbidity");
    public static final Parameter WIND_SPEED = new Parameter("Wind Speed");
    public static final Parameter PRESSURE = new Parameter("Pressure");

    private final String dssString;

    private Parameter(String dssString) {
        this.dssString = Objects.requireNonNull(dssString);
        if (dssString.isBlank()) {
            throw new IllegalArgumentException("Parameter name must not be blank");
        }
    }

    /** Creates a parameter from a DSS string. Use constants when possible. */
    public static Parameter of(String dssString) {
        return new Parameter(dssString);
    }

    /** Returns the DSS-native string representation. */
    public String dssString() { return dssString; }

    @Override
    public String toString() { return dssString; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Parameter p && dssString.equalsIgnoreCase(p.dssString));
    }

    @Override
    public int hashCode() {
        return dssString.toLowerCase().hashCode();
    }
}

package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Array data read from or written to a DSS file.
 * A single record can contain int, float, and/or double arrays.
 * Empty arrays (length 0) indicate that type is not present.
 */
public final class DssArray {
    private final int[] intValues;
    private final float[] floatValues;
    private final double[] doubleValues;

    public DssArray(int[] intValues, float[] floatValues, double[] doubleValues) {
        this.intValues = Objects.requireNonNull(intValues);
        this.floatValues = Objects.requireNonNull(floatValues);
        this.doubleValues = Objects.requireNonNull(doubleValues);
    }

    public int[] intValues() { return intValues; }
    public float[] floatValues() { return floatValues; }
    public double[] doubleValues() { return doubleValues; }
}

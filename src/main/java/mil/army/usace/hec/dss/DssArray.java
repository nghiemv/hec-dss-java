package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Array data read from or written to a DSS file.
 * A single record can contain int, float, and/or double arrays.
 * Empty arrays (length 0) indicate that type is not present.
 */
public record DssArray(int[] intValues, float[] floatValues, double[] doubleValues) {
    public DssArray {
        intValues = Objects.requireNonNull(intValues).clone();
        floatValues = Objects.requireNonNull(floatValues).clone();
        doubleValues = Objects.requireNonNull(doubleValues).clone();
    }

    @Override public int[] intValues() { return intValues.clone(); }
    @Override public float[] floatValues() { return floatValues.clone(); }
    @Override public double[] doubleValues() { return doubleValues.clone(); }
}

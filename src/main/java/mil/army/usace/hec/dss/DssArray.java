package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Array data read from or written to a DSS file.
 */
public record DssArray(double[] values) {
    public DssArray {
        values = Objects.requireNonNull(values).clone();
    }

    @Override public double[] values() { return values.clone(); }
}

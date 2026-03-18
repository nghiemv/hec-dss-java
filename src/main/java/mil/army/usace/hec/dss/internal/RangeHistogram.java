package mil.army.usace.hec.dss.internal;

import java.util.Objects;

/**
 * Histogram of cell values used by the native DSS library for compression.
 * Not exposed in the public API.
 */
public record RangeHistogram(double[] limits, int[] exceedanceCounts) {
    public static final RangeHistogram EMPTY = new RangeHistogram(new double[0], new int[0]);

    public RangeHistogram {
        limits = Objects.requireNonNull(limits).clone();
        exceedanceCounts = Objects.requireNonNull(exceedanceCounts).clone();
    }

    @Override public double[] limits() { return limits.clone(); }
    @Override public int[] exceedanceCounts() { return exceedanceCounts.clone(); }

    public int size() { return limits.length; }
}

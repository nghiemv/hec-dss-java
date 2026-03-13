package mil.army.usace.hec.dss;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Time series data read from a DSS file.
 * This is a plain value object — no native resources, no lifecycle management.
 * Safe to hold indefinitely, pass between threads, serialize, etc.
 */
public final class DssTimeSeries {
    private final double[] values;
    private final long[] epochSeconds;
    private final String units;
    private final String type;

    public DssTimeSeries(double[] values, long[] epochSeconds, String units, String type) {
        this.values = Objects.requireNonNull(values);
        this.epochSeconds = Objects.requireNonNull(epochSeconds);
        this.units = Objects.requireNonNull(units);
        this.type = Objects.requireNonNull(type);
        if (values.length != epochSeconds.length) {
            throw new IllegalArgumentException(
                    "values length (%d) != times length (%d)".formatted(values.length, epochSeconds.length));
        }
    }

    public int size() {
        return values.length;
    }

    public double value(int index) {
        Objects.checkIndex(index, values.length);
        return values[index];
    }

    public Instant time(int index) {
        Objects.checkIndex(index, epochSeconds.length);
        return Instant.ofEpochSecond(epochSeconds[index]);
    }

    /**
     * Returns the backing values array. Do not mutate.
     */
    public double[] values() {
        return values;
    }

    /**
     * Returns the epoch second at the given index (no object allocation).
     */
    public long epochSecond(int index) {
        Objects.checkIndex(index, epochSeconds.length);
        return epochSeconds[index];
    }

    /**
     * Returns the backing epoch-seconds array. Do not mutate.
     */
    public long[] epochSeconds() {
        return epochSeconds;
    }

    public String units() {
        return units;
    }

    public String type() {
        return type;
    }

    /**
     * Returns a new time series with missing/undefined values excluded.
     */
    public DssTimeSeries dropNa() {
        int[] kept = IntStream.range(0, values.length)
                .filter(i -> values[i] != DssConstants.UNDEFINED_DOUBLE)
                .toArray();
        if (kept.length == values.length) return this;
        double[] newValues = new double[kept.length];
        long[] newTimes = new long[kept.length];
        for (int i = 0; i < kept.length; i++) {
            newValues[i] = values[kept[i]];
            newTimes[i] = epochSeconds[kept[i]];
        }
        return new DssTimeSeries(newValues, newTimes, units, type);
    }
}

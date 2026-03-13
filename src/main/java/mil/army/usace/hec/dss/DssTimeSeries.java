package mil.army.usace.hec.dss;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Time series data read from or written to a DSS file.
 * Plain value object — no native resources, no lifecycle management.
 *
 * <p>Missing values are represented as {@link Double#NaN}.
 * Use {@link #isUndefined(int)} to check, or {@link #dropNa()} to exclude them.
 */
public final class DssTimeSeries {
    private final double[] values;
    private final long[] epochSeconds;
    private final String units;
    private final String type;

    public DssTimeSeries(Instant[] times, double[] values, String units, String type) {
        Objects.requireNonNull(times);
        Objects.requireNonNull(values);
        if (values.length != times.length) {
            throw new IllegalArgumentException(
                    "values length (%d) != times length (%d)".formatted(values.length, times.length));
        }
        this.epochSeconds = new long[times.length];
        for (int i = 0; i < times.length; i++) {
            this.epochSeconds[i] = Objects.requireNonNull(times[i]).getEpochSecond();
        }
        this.values = values.clone();
        this.units = Objects.requireNonNull(units);
        this.type = Objects.requireNonNull(type);
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

    public double[] values() {
        return values.clone();
    }

    public Instant[] times() {
        Instant[] result = new Instant[epochSeconds.length];
        for (int i = 0; i < epochSeconds.length; i++) {
            result[i] = Instant.ofEpochSecond(epochSeconds[i]);
        }
        return result;
    }

    public String units() {
        return units;
    }

    public String type() {
        return type;
    }

    /**
     * Returns true if the value at the given index is undefined (missing).
     */
    public boolean isUndefined(int index) {
        Objects.checkIndex(index, values.length);
        return Double.isNaN(values[index]);
    }

    /**
     * Returns a new time series with missing/undefined values excluded.
     */
    public DssTimeSeries dropNa() {
        int[] kept = IntStream.range(0, values.length)
                .filter(i -> !Double.isNaN(values[i]))
                .toArray();
        if (kept.length == values.length) return this;
        double[] newValues = new double[kept.length];
        Instant[] newTimes = new Instant[kept.length];
        for (int i = 0; i < kept.length; i++) {
            newValues[i] = values[kept[i]];
            newTimes[i] = Instant.ofEpochSecond(epochSeconds[kept[i]]);
        }
        return new DssTimeSeries(newTimes, newValues, units, type);
    }
}

package mil.army.usace.hec.dss;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Time series data read from or written to a DSS file.
 * Plain value object — no native resources, no lifecycle management.
 *
 * <p>Missing values are represented as {@link Double#NaN}.
 * Use {@link #isUndefined(int)} to check, or {@link #dropNa()} to exclude them.
 *
 * <p>Times are stored as UTC {@link Instant}s. The optional {@link #timeZone()}
 * records the time zone the data was originally observed in — DSS stores this
 * per-record but it does not affect the timestamps themselves.
 */
public final class DssTimeSeries {
    private final double[] values;
    private final long[] epochSeconds;
    private final String units;
    private final TimeSeriesDataType type;
    private final ZoneId timeZone;

    /**
     * Creates a time series with a time zone.
     */
    public static DssTimeSeries of(Instant[] times, double[] values, String units,
                                   TimeSeriesDataType type, ZoneId timeZone) {
        return new DssTimeSeries(times, values, units, type, timeZone);
    }

    /**
     * Creates a time series with no time zone.
     */
    public static DssTimeSeries of(Instant[] times, double[] values, String units,
                                   TimeSeriesDataType type) {
        return new DssTimeSeries(times, values, units, type, null);
    }

    public DssTimeSeries(Instant[] times, double[] values, String units,
                         TimeSeriesDataType type, ZoneId timeZone) {
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
        this.timeZone = timeZone;
    }

    public DssTimeSeries(Instant[] times, double[] values, String units, TimeSeriesDataType type) {
        this(times, values, units, type, null);
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

    public TimeSeriesDataType type() {
        return type;
    }

    /**
     * Returns the time zone the data was observed in, or null if not specified.
     * This is metadata only — it does not affect the UTC timestamps.
     */
    public ZoneId timeZone() {
        return timeZone;
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
        return new DssTimeSeries(newTimes, newValues, units, type, timeZone);
    }
}

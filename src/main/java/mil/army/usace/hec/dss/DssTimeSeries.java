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
 * <p>Times are true UTC {@link Instant}s. When reading from DSS, the stored
 * timezone is used to convert the native calendar times to UTC. When writing,
 * Instants are converted to the specified timezone's local time for native
 * storage. If no timezone is stored or specified, UTC is assumed.
 *
 * <p>Quality flags are optional per-value integers stored by DSS. The meaning
 * of individual bits is application-defined (e.g. screened, valid, missing,
 * rejected). Use {@link #quality(int)} to access per-value flags, or
 * {@link #hasQuality()} to check if quality data is present.
 */
public final class DssTimeSeries {
    private final double[] values;
    private final long[] epochSeconds;
    private final String units;
    private final TimeSeriesDataType type;
    private final ZoneId timeZone;
    private final int[] quality;

    /**
     * Creates a time series with timezone and quality flags.
     */
    public static DssTimeSeries of(Instant[] times, double[] values, String units,
                                   TimeSeriesDataType type, ZoneId timeZone, int[] quality) {
        return new DssTimeSeries(times, values, units, type, timeZone, quality);
    }

    /**
     * Creates a time series with a time zone.
     */
    public static DssTimeSeries of(Instant[] times, double[] values, String units,
                                   TimeSeriesDataType type, ZoneId timeZone) {
        return new DssTimeSeries(times, values, units, type, timeZone, null);
    }

    /**
     * Creates a time series with no time zone or quality flags.
     */
    public static DssTimeSeries of(Instant[] times, double[] values, String units,
                                   TimeSeriesDataType type) {
        return new DssTimeSeries(times, values, units, type, null, null);
    }

    public DssTimeSeries(Instant[] times, double[] values, String units,
                         TimeSeriesDataType type, ZoneId timeZone, int[] quality) {
        Objects.requireNonNull(times);
        Objects.requireNonNull(values);
        if (values.length != times.length) {
            throw new IllegalArgumentException(
                    "values length (%d) != times length (%d)".formatted(values.length, times.length));
        }
        if (quality != null && quality.length != values.length) {
            throw new IllegalArgumentException(
                    "quality length (%d) != values length (%d)".formatted(quality.length, values.length));
        }
        this.epochSeconds = new long[times.length];
        for (int i = 0; i < times.length; i++) {
            this.epochSeconds[i] = Objects.requireNonNull(times[i]).getEpochSecond();
        }
        this.values = values.clone();
        this.units = Objects.requireNonNull(units);
        this.type = Objects.requireNonNull(type);
        this.timeZone = timeZone;
        this.quality = quality != null ? quality.clone() : null;
    }

    public DssTimeSeries(Instant[] times, double[] values, String units,
                         TimeSeriesDataType type, ZoneId timeZone) {
        this(times, values, units, type, timeZone, null);
    }

    public DssTimeSeries(Instant[] times, double[] values, String units, TimeSeriesDataType type) {
        this(times, values, units, type, null, null);
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
     * When present, this timezone was used to convert between DSS native calendar
     * times and the UTC Instants in this object.
     */
    public ZoneId timeZone() {
        return timeZone;
    }

    /** Returns true if this time series has quality flag data. */
    public boolean hasQuality() {
        return quality != null;
    }

    /**
     * Returns the quality flag for the value at the given index.
     * Quality flags are application-defined bit fields (0 typically means no flags).
     *
     * @throws IllegalStateException if no quality data is present ({@link #hasQuality()} is false)
     */
    public int quality(int index) {
        if (quality == null) {
            throw new IllegalStateException("No quality data present");
        }
        Objects.checkIndex(index, quality.length);
        return quality[index];
    }

    /**
     * Returns a copy of all quality flags, or null if no quality data is present.
     */
    public int[] qualityFlags() {
        return quality != null ? quality.clone() : null;
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
        int[] newQuality = quality != null ? new int[kept.length] : null;
        for (int i = 0; i < kept.length; i++) {
            newValues[i] = values[kept[i]];
            newTimes[i] = Instant.ofEpochSecond(epochSeconds[kept[i]]);
            if (newQuality != null) {
                newQuality[i] = quality[kept[i]];
            }
        }
        return new DssTimeSeries(newTimes, newValues, units, type, timeZone, newQuality);
    }
}

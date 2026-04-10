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
 * Use {@link #isMissing(int)} to check, or {@link #dropMissing()} to exclude them.
 *
 * <p>Times are true UTC {@link Instant}s. When reading from DSS, the stored
 * timezone is used to convert the native calendar times to UTC. When writing,
 * Instants are converted to the specified timezone's local time for native
 * storage. If no timezone is stored or specified, UTC is assumed.
 *
 * <p><b>Precision note:</b> DSS stores time series at second precision.
 * Sub-second components of {@link Instant} inputs are truncated on construction.
 * Round-tripping an {@code Instant} with nanoseconds will lose them.
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

    private DssTimeSeries(Instant[] times, double[] values, String units,
                          TimeSeriesDataType type, ZoneId timeZone, int[] quality) {
        Objects.requireNonNull(times, "times must not be null");
        Objects.requireNonNull(values, "values must not be null");
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
            this.epochSeconds[i] = Objects.requireNonNull(times[i], "times must not contain null").getEpochSecond();
        }
        this.values = values.clone();
        this.units = Objects.requireNonNull(units, "units must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.timeZone = timeZone;
        this.quality = quality != null ? quality.clone() : null;
    }

    /** Number of values in the series. */
    public int size() {
        return values.length;
    }

    /**
     * Returns the value at the given index. Missing values are {@link Double#NaN}.
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public double value(int index) {
        Objects.checkIndex(index, values.length);
        return values[index];
    }

    /**
     * Returns the time at the given index (at second precision).
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public Instant time(int index) {
        Objects.checkIndex(index, epochSeconds.length);
        return Instant.ofEpochSecond(epochSeconds[index]);
    }

    /** Returns a defensive copy of all values. Missing values are {@link Double#NaN}. */
    public double[] values() {
        return values.clone();
    }

    /** Returns a defensive copy of all times (at second precision). */
    public Instant[] times() {
        Instant[] result = new Instant[epochSeconds.length];
        for (int i = 0; i < epochSeconds.length; i++) {
            result[i] = Instant.ofEpochSecond(epochSeconds[i]);
        }
        return result;
    }

    /** Data units (e.g. {@code "CFS"}, {@code "CMS"}). */
    public String units() {
        return units;
    }

    /** What the values represent over time (instantaneous, cumulative, period-average, …). */
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
     * Returns a defensive copy of all quality flags, or {@code null} if
     * no quality data is present.
     */
    public int[] quality() {
        return quality != null ? quality.clone() : null;
    }

    /**
     * Returns true if the value at the given index is missing (NaN).
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    public boolean isMissing(int index) {
        Objects.checkIndex(index, values.length);
        return Double.isNaN(values[index]);
    }

    @Override
    public String toString() {
        String first = size() > 0 ? Instant.ofEpochSecond(epochSeconds[0]).toString() : "-";
        String last = size() > 0 ? Instant.ofEpochSecond(epochSeconds[size() - 1]).toString() : "-";
        return "DssTimeSeries[size=%d, units=%s, type=%s, tz=%s, from=%s, to=%s, quality=%s]"
                .formatted(size(), units, type, timeZone, first, last, hasQuality());
    }

    /**
     * Returns a new time series with missing values excluded.
     * Returns this same instance if nothing is missing.
     */
    public DssTimeSeries dropMissing() {
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

package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Paired data (x/y curves) read from or written to a DSS file.
 * A single x-axis ("ordinates") shared across one or more y-curves.
 *
 * <p>Access curve data through {@link #curve(int)} (whole curve),
 * {@link #curves()} (all curves), or {@link #yValue(int, int)} (single cell).
 *
 * <p>Missing values are represented as {@link Double#NaN}.
 */
public final class DssPairedData {
    private final double[] ordinates;
    private final double[] flatValues; // row-major: flatValues[i * curveCount + c]
    private final int curveCount;
    private final String[] labels;
    private final String xUnits;
    private final String yUnits;
    private final String xType;
    private final String yType;

    private DssPairedData(double[] ordinates, double[][] curves, String[] labels,
                          String xUnits, String yUnits, String xType, String yType) {
        Objects.requireNonNull(ordinates, "ordinates must not be null");
        Objects.requireNonNull(curves, "curves must not be null");
        if (curves.length < 1) {
            throw new IllegalArgumentException("Must have at least one curve");
        }
        for (int c = 0; c < curves.length; c++) {
            Objects.requireNonNull(curves[c], "curves must not contain null");
            if (curves[c].length != ordinates.length) {
                throw new IllegalArgumentException(
                        "Curve %d length (%d) != ordinates length (%d)"
                                .formatted(c, curves[c].length, ordinates.length));
            }
        }
        this.ordinates = ordinates.clone();
        this.curveCount = curves.length;
        this.flatValues = new double[ordinates.length * curveCount];
        for (int i = 0; i < ordinates.length; i++) {
            for (int c = 0; c < curveCount; c++) {
                flatValues[i * curveCount + c] = curves[c][i];
            }
        }
        this.labels = labels != null ? labels.clone() : new String[0];
        this.xUnits = Objects.requireNonNull(xUnits, "xUnits must not be null");
        this.yUnits = Objects.requireNonNull(yUnits, "yUnits must not be null");
        this.xType = Objects.requireNonNull(xType, "xType must not be null");
        this.yType = Objects.requireNonNull(yType, "yType must not be null");
    }

    /**
     * Creates single-curve paired data with no labels.
     */
    public static DssPairedData of(double[] x, double[] y,
                                   String xUnits, String yUnits,
                                   String xType, String yType) {
        return new DssPairedData(x, new double[][]{y}, null, xUnits, yUnits, xType, yType);
    }

    /**
     * Creates multi-curve paired data.
     *
     * @param ordinates x-values shared by all curves
     * @param curves    y-values per curve; {@code curves[c]} is the y-array for curve {@code c}
     * @param labels    curve labels (may be empty or {@code null})
     */
    public static DssPairedData of(double[] ordinates, double[][] curves, String[] labels,
                                   String xUnits, String yUnits, String xType, String yType) {
        return new DssPairedData(ordinates, curves, labels, xUnits, yUnits, xType, yType);
    }

    /** Number of ordinate (x) values, shared across all curves. */
    public int ordinateCount() { return ordinates.length; }

    /** Number of y-curves. */
    public int curveCount() { return curveCount; }

    /** Defensive copy of the ordinates (x-values). */
    public double[] ordinates() { return ordinates.clone(); }

    /** Defensive copy of the curve labels. Empty array if no labels are present. */
    public String[] labels() { return labels.clone(); }

    /** Units of the x-axis. */
    public String xUnits() { return xUnits; }

    /** Units of the y-axis. */
    public String yUnits() { return yUnits; }

    /** Parameter type of the x-axis (e.g. {@code "Stage"}). */
    public String xType() { return xType; }

    /** Parameter type of the y-axis (e.g. {@code "Flow"}). */
    public String yType() { return yType; }

    /**
     * Returns the y-value at the given ordinate index for the given curve.
     * Missing values are {@link Double#NaN}.
     *
     * @throws IndexOutOfBoundsException if either index is out of range
     */
    public double yValue(int ordinateIndex, int curveIndex) {
        Objects.checkIndex(ordinateIndex, ordinates.length);
        Objects.checkIndex(curveIndex, curveCount);
        return flatValues[ordinateIndex * curveCount + curveIndex];
    }

    /**
     * Returns a defensive copy of all y-values for the given curve.
     *
     * @throws IndexOutOfBoundsException if {@code curveIndex} is out of range
     */
    public double[] curve(int curveIndex) {
        Objects.checkIndex(curveIndex, curveCount);
        double[] result = new double[ordinates.length];
        for (int i = 0; i < ordinates.length; i++) {
            result[i] = flatValues[i * curveCount + curveIndex];
        }
        return result;
    }

    /**
     * Returns a defensive copy of all curves as a 2D array.
     * {@code curves()[c]} is the y-array for curve {@code c}.
     */
    public double[][] curves() {
        double[][] result = new double[curveCount][ordinates.length];
        for (int c = 0; c < curveCount; c++) {
            for (int i = 0; i < ordinates.length; i++) {
                result[c][i] = flatValues[i * curveCount + c];
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "DssPairedData[ordinates=%d, curves=%d, xUnits=%s, yUnits=%s, xType=%s, yType=%s]"
                .formatted(ordinates.length, curveCount, xUnits, yUnits, xType, yType);
    }
}

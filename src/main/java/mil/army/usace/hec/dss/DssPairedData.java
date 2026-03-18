package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Paired data (x/y curves) read from or written to a DSS file.
 * Access y-values through {@link #curve(int)} or {@link #yValue(int, int)}.
 */
public final class DssPairedData {
    private final double[] ordinates;
    private final double[] flatValues; // column-major: flatValues[i * numberCurves + c]
    private final int numberCurves;
    private final String[] labels;
    private final String xUnits;
    private final String yUnits;
    private final Parameter xType;
    private final Parameter yType;

    /**
     * Creates paired data from ordinates and per-curve y-value arrays.
     *
     * @param ordinates x-values shared by all curves
     * @param curves    y-values per curve; curves[c] is the y-array for curve c
     * @param labels    curve labels (may be empty or null)
     */
    public DssPairedData(double[] ordinates, double[][] curves, String[] labels,
                         String xUnits, String yUnits, Parameter xType, Parameter yType) {
        Objects.requireNonNull(ordinates);
        Objects.requireNonNull(curves);
        if (curves.length < 1) {
            throw new IllegalArgumentException("Must have at least one curve");
        }
        for (int c = 0; c < curves.length; c++) {
            if (curves[c].length != ordinates.length) {
                throw new IllegalArgumentException(
                        "Curve %d length (%d) != ordinates length (%d)"
                                .formatted(c, curves[c].length, ordinates.length));
            }
        }
        this.ordinates = ordinates.clone();
        this.numberCurves = curves.length;
        this.flatValues = new double[ordinates.length * numberCurves];
        for (int i = 0; i < ordinates.length; i++) {
            for (int c = 0; c < numberCurves; c++) {
                flatValues[i * numberCurves + c] = curves[c][i];
            }
        }
        this.labels = labels != null ? labels.clone() : new String[0];
        this.xUnits = Objects.requireNonNull(xUnits);
        this.yUnits = Objects.requireNonNull(yUnits);
        this.xType = Objects.requireNonNull(xType);
        this.yType = Objects.requireNonNull(yType);
    }

    /**
     * Creates single-curve paired data with no labels.
     */
    public static DssPairedData of(double[] x, double[] y,
                                   String xUnits, String yUnits,
                                   Parameter xType, Parameter yType) {
        return new DssPairedData(x, new double[][]{y}, null, xUnits, yUnits, xType, yType);
    }

    public int numberOrdinates() { return ordinates.length; }
    public int numberCurves() { return numberCurves; }
    public double[] ordinates() { return ordinates.clone(); }
    public String[] labels() { return labels.clone(); }
    public String xUnits() { return xUnits; }
    public String yUnits() { return yUnits; }
    public Parameter xType() { return xType; }
    public Parameter yType() { return yType; }

    /**
     * Returns the y-value at the given ordinate index for the given curve.
     */
    public double yValue(int ordinateIndex, int curveIndex) {
        Objects.checkIndex(ordinateIndex, ordinates.length);
        Objects.checkIndex(curveIndex, numberCurves);
        return flatValues[ordinateIndex * numberCurves + curveIndex];
    }

    /**
     * Returns all y-values for the given curve (defensive copy).
     */
    public double[] curve(int curveIndex) {
        Objects.checkIndex(curveIndex, numberCurves);
        double[] result = new double[ordinates.length];
        for (int i = 0; i < ordinates.length; i++) {
            result[i] = flatValues[i * numberCurves + curveIndex];
        }
        return result;
    }
}

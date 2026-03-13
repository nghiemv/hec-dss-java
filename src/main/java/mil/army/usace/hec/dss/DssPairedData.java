package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Paired data (x/y curves) read from or written to a DSS file.
 * Values are stored column-major: for multi-curve data, values[i * numberCurves + c]
 * gives the y-value at ordinate i for curve c.
 */
public final class DssPairedData {
    private final double[] ordinates;
    private final double[] values;
    private final int numberCurves;
    private final String[] labels;
    private final String xUnits;
    private final String yUnits;
    private final String xType;
    private final String yType;

    public DssPairedData(double[] ordinates, double[] values, int numberCurves,
                         String[] labels, String xUnits, String yUnits,
                         String xType, String yType) {
        this.ordinates = Objects.requireNonNull(ordinates);
        this.values = Objects.requireNonNull(values);
        this.numberCurves = numberCurves;
        this.labels = labels != null ? labels : new String[0];
        this.xUnits = Objects.requireNonNull(xUnits);
        this.yUnits = Objects.requireNonNull(yUnits);
        this.xType = Objects.requireNonNull(xType);
        this.yType = Objects.requireNonNull(yType);
        if (numberCurves < 1) {
            throw new IllegalArgumentException("numberCurves must be >= 1, got " + numberCurves);
        }
        if (ordinates.length * numberCurves != values.length) {
            throw new IllegalArgumentException(
                    "values length (%d) != ordinates (%d) * curves (%d)"
                            .formatted(values.length, ordinates.length, numberCurves));
        }
    }

    public int numberOrdinates() { return ordinates.length; }
    public int numberCurves() { return numberCurves; }
    public double[] ordinates() { return ordinates; }
    public double[] values() { return values; }
    public String[] labels() { return labels; }
    public String xUnits() { return xUnits; }
    public String yUnits() { return yUnits; }
    public String xType() { return xType; }
    public String yType() { return yType; }
}

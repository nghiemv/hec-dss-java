package mil.army.usace.hec.dss;

import mil.army.usace.hec.dss.internal.NativeGridMetadata;

import java.util.Arrays;
import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 *
 * <p>A grid has five essential concepts:
 * <ul>
 *   <li>{@link #values()} — cell data as {@code double[][]}, row-major, row 0 = north</li>
 *   <li>{@link #x()} — column center coordinates (west → east)</li>
 *   <li>{@link #y()} — row center coordinates (north → south)</li>
 *   <li>{@link #units()} — data units (e.g. "MM", "IN")</li>
 *   <li>{@link #crs()} — coordinate reference system</li>
 * </ul>
 *
 * <p>Missing cell values are represented as {@link Double#NaN}.
 *
 * <p>Create grids with {@link #of(double[][], double[], double[], String, Crs)}:
 * <pre>{@code
 * DssGrid grid = DssGrid.of(values, x, y, "MM", Crs.SHG);
 * }</pre>
 */
public final class DssGrid {
    private final double[][] values;
    private final double[] x;
    private final double[] y;
    private final String units;
    private final Crs crs;
    private final GridDataType dataType;
    private final NativeGridMetadata nativeMetadata; // null for user-constructed grids

    private DssGrid(double[][] values, double[] x, double[] y,
                    String units, Crs crs, GridDataType dataType,
                    NativeGridMetadata nativeMetadata) {
        this.values = deepCopy(Objects.requireNonNull(values));
        this.x = Objects.requireNonNull(x).clone();
        this.y = Objects.requireNonNull(y).clone();
        this.units = Objects.requireNonNull(units);
        this.crs = Objects.requireNonNull(crs);
        this.dataType = Objects.requireNonNull(dataType);
        this.nativeMetadata = nativeMetadata;

        if (values.length > 0 && values[0].length != x.length) {
            throw new IllegalArgumentException(
                    "x length (%d) must match column count (%d)".formatted(x.length, values[0].length));
        }
        if (values.length != y.length) {
            throw new IllegalArgumentException(
                    "y length (%d) must match row count (%d)".formatted(y.length, values.length));
        }
    }

    /**
     * Creates a grid from user data.
     *
     * @param values cell data, row-major, row 0 = north
     * @param x      column center coordinates (west → east)
     * @param y      row center coordinates (north → south)
     * @param units  data units (e.g. "MM")
     * @param crs    coordinate reference system
     */
    public static DssGrid of(double[][] values, double[] x, double[] y,
                              String units, Crs crs) {
        return new DssGrid(values, x, y, units, crs, GridDataType.PERIOD_AVERAGE, null);
    }

    /**
     * Internal factory for GridReader — preserves native metadata for round-trip.
     * Not part of the public API.
     */
    public static DssGrid fromNative(double[][] values, double[] x, double[] y,
                                     String units, Crs crs, GridDataType dataType,
                                     NativeGridMetadata nativeMetadata) {
        return new DssGrid(values, x, y, units, crs, dataType, nativeMetadata);
    }

    // ---- Core data ----

    /** Cell data as row-major 2D array. Row 0 = north, row {@code height()-1} = south. */
    public double[][] values() { return deepCopy(values); }

    /** Column center coordinates, west → east. Length = {@link #width()}. */
    public double[] x() { return x.clone(); }

    /** Row center coordinates, north → south. Length = {@link #height()}. */
    public double[] y() { return y.clone(); }

    /** Data units (e.g. "MM", "IN"). */
    public String units() { return units; }

    /** Coordinate reference system. */
    public Crs crs() { return crs; }

    /** What the cell values represent over time. */
    public GridDataType dataType() { return dataType; }

    // ---- Convenience ----

    /** Number of columns. */
    public int width() { return x.length; }

    /** Number of rows. */
    public int height() { return y.length; }

    /** Single cell value. Row 0 = north. */
    public double value(int row, int col) { return values[row][col]; }

    /** Cell spacing. Derived from x coordinates. Returns 0 if fewer than 2 columns. */
    public double cellSize() {
        return x.length >= 2 ? Math.abs(x[1] - x[0]) : 0;
    }

    // ---- Internal ----

    /** Not part of the public API. Used by GridWriter for round-trip fidelity. */
    public NativeGridMetadata nativeMetadata() { return nativeMetadata; }

    private static double[][] deepCopy(double[][] src) {
        double[][] copy = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}

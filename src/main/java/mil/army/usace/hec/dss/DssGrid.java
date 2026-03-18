package mil.army.usace.hec.dss;

import mil.army.usace.hec.dss.internal.NativeGridMetadata;

import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 *
 * <p>A grid stores cell values on a regular (uniform-spacing) grid defined by:
 * <ul>
 *   <li>{@link #width()}, {@link #height()} — grid dimensions in cells</li>
 *   <li>{@link #cellSize()} — cell spacing in CRS units</li>
 *   <li>{@link #xOrigin()}, {@link #yOrigin()} — west and south edges of the grid</li>
 * </ul>
 *
 * <p>Cell coordinates are derived from the geometry — use {@link #x(int)} and {@link #y(int)}
 * to get the center coordinate of any cell in O(1).
 *
 * <p>Missing cell values are represented as {@link Double#NaN}.
 *
 * <p><b>Precision note:</b> DSS currently stores grid data as 32-bit floats
 * ({@code GRID_FLOAT} in the native library), though the format has infrastructure
 * for double storage ({@code GRID_DOUBLE}). This API uses doubles so it will not
 * require a breaking change if/when double storage is enabled. Until then, values
 * lose precision beyond ~7 significant digits on round-trip.
 *
 * <p>Create grids from scratch:
 * <pre>{@code
 * DssGrid grid = DssGrid.of(data, 50, 50, 2000.0, 0.0, 0.0,
 *         "MM", Crs.SHG, GridDataType.PER_CUM);
 * }</pre>
 *
 * <p>Or derive from an existing grid (common case — same geometry, new data):
 * <pre>{@code
 * DssGrid output = sourceGrid.withData(newData, "MM", GridDataType.PER_CUM);
 * }</pre>
 */
public final class DssGrid {
    private final double[] data;
    private final int width;
    private final int height;
    private final double cellSize;
    private final double xOrigin;
    private final double yOrigin;
    private final String units;
    private final Crs crs;
    private final GridDataType dataType;
    private final NativeGridMetadata nativeMetadata;

    private DssGrid(double[] data, int width, int height,
                    double cellSize, double xOrigin, double yOrigin,
                    String units, Crs crs, GridDataType dataType,
                    NativeGridMetadata nativeMetadata) {
        this.data = Objects.requireNonNull(data).clone();
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.units = Objects.requireNonNull(units);
        this.crs = Objects.requireNonNull(crs);
        this.dataType = Objects.requireNonNull(dataType);
        this.nativeMetadata = nativeMetadata;

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "dimensions must be positive: width=%d, height=%d".formatted(width, height));
        }
        if (cellSize <= 0) {
            throw new IllegalArgumentException(
                    "cellSize must be positive: %f".formatted(cellSize));
        }
        if (data.length != width * height) {
            throw new IllegalArgumentException(
                    "data length (%d) must equal width * height (%d)".formatted(data.length, width * height));
        }
    }

    /**
     * Creates a grid from scratch.
     *
     * @param data     flat row-major cell data (row 0 = north), length must equal width * height
     * @param width    number of columns
     * @param height   number of rows
     * @param cellSize cell spacing in CRS units (must be positive)
     * @param xOrigin  west edge of the grid
     * @param yOrigin  south edge of the grid
     * @param units    data units
     * @param crs      coordinate reference system
     * @param dataType what the cell values represent over time
     */
    public static DssGrid of(double[] data, int width, int height,
                             double cellSize, double xOrigin, double yOrigin,
                             String units, Crs crs, GridDataType dataType) {
        return new DssGrid(data, width, height, cellSize, xOrigin, yOrigin,
                units, crs, dataType, null);
    }

    /**
     * Returns a new grid with the same geometry and CRS but different data, units, and data type.
     * This is the common case: applying model results to an existing grid's spatial layout.
     */
    public DssGrid withData(double[] newData, String units, GridDataType dataType) {
        return new DssGrid(newData, width, height, cellSize, xOrigin, yOrigin,
                units, crs, dataType, null);
    }

    /**
     * Returns a new grid with different units.
     */
    public DssGrid withUnits(String units) {
        return new DssGrid(data, width, height, cellSize, xOrigin, yOrigin,
                units, crs, dataType, nativeMetadata);
    }

    /**
     * Returns a new grid with a different data type.
     */
    public DssGrid withDataType(GridDataType dataType) {
        return new DssGrid(data, width, height, cellSize, xOrigin, yOrigin,
                units, crs, dataType, nativeMetadata);
    }

    // ---- Data access ----

    /** Single cell value. Row 0 = north. */
    public double value(int row, int col) { return data[row * width + col]; }

    /** Flat copy of cell data, row-major, row 0 = north. */
    public double[] data() { return data.clone(); }

    // ---- Geometry ----

    /** Number of columns. */
    public int width() { return width; }

    /** Number of rows. */
    public int height() { return height; }

    /** Cell spacing in CRS units. */
    public double cellSize() { return cellSize; }

    /** West edge of the grid. */
    public double xOrigin() { return xOrigin; }

    /** South edge of the grid. */
    public double yOrigin() { return yOrigin; }

    /** Center x-coordinate of the given column. */
    public double x(int col) { return xOrigin + (col + 0.5) * cellSize; }

    /** Center y-coordinate of the given row. Row 0 = north. */
    public double y(int row) { return yOrigin + (height - 1 - row + 0.5) * cellSize; }

    // ---- Semantics ----

    /** Data units. */
    public String units() { return units; }

    /** Coordinate reference system. */
    public Crs crs() { return crs; }

    /** What the cell values represent over time. */
    public GridDataType dataType() { return dataType; }

    // ---- Internal (used by GridReader/GridWriter — not part of the public API) ----

    /** @hidden */
    public NativeGridMetadata nativeMetadata() { return nativeMetadata; }

    /** @hidden */
    public static DssGrid fromNative(double[] data, int width, int height,
                                     double cellSize, double xOrigin, double yOrigin,
                                     String units, Crs crs, GridDataType dataType,
                                     NativeGridMetadata nativeMetadata) {
        return new DssGrid(data, width, height, cellSize, xOrigin, yOrigin,
                units, crs, dataType, nativeMetadata);
    }
}

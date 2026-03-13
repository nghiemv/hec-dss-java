package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 * Contains cell data, grid dimensions, spatial reference, and statistics.
 *
 * <p>Missing cell values are represented as {@link Double#NaN}.
 *
 * <p>Create grids with {@link #of(double[], int, int, double, double, double)}:
 * <pre>{@code
 * DssGrid grid = DssGrid.of(data, 50, 50, 2000.0, 0.0, 0.0)
 *         .withSrs("WKT", wktString)
 *         .withTimeZone("UTC");
 * }</pre>
 */
public final class DssGrid {
    private final double[] data;

    // Grid info
    private final GridType gridType;
    private final GridDataType dataType;
    private final int lowerLeftCellX;
    private final int lowerLeftCellY;
    private final int numberOfCellsX;
    private final int numberOfCellsY;
    private final double cellSize;
    private final double xCoordOfGridCellZero;
    private final double yCoordOfGridCellZero;
    private final boolean isInterval;
    private final boolean isTimeStamped;
    private final String timeZoneId;

    // Spatial reference
    private final String srsName;
    private final String srsDefinition;
    private final int srsDefinitionType;

    // Statistics
    private final double maxDataValue;
    private final double minDataValue;
    private final double meanDataValue;
    private final RangeHistogram rangeHistogram;

    /**
     * Histogram of cell values used by the native DSS library for compression.
     */
    public record RangeHistogram(double[] limits, int[] exceedanceCounts) {
        public static final RangeHistogram EMPTY = new RangeHistogram(new double[0], new int[0]);

        public RangeHistogram {
            limits = Objects.requireNonNull(limits).clone();
            exceedanceCounts = Objects.requireNonNull(exceedanceCounts).clone();
        }

        @Override public double[] limits() { return limits.clone(); }
        @Override public int[] exceedanceCounts() { return exceedanceCounts.clone(); }

        public int size() { return limits.length; }
    }

    public DssGrid(double[] data,
                   GridType gridType, GridDataType dataType,
                   int lowerLeftCellX, int lowerLeftCellY,
                   int numberOfCellsX, int numberOfCellsY,
                   double cellSize, double xCoordOfGridCellZero, double yCoordOfGridCellZero,
                   boolean isInterval, boolean isTimeStamped,
                   String timeZoneId,
                   String srsName, String srsDefinition, int srsDefinitionType,
                   double maxDataValue, double minDataValue, double meanDataValue,
                   RangeHistogram rangeHistogram) {
        this.data = Objects.requireNonNull(data).clone();
        this.gridType = Objects.requireNonNull(gridType);
        this.dataType = Objects.requireNonNull(dataType);
        this.lowerLeftCellX = lowerLeftCellX;
        this.lowerLeftCellY = lowerLeftCellY;
        this.numberOfCellsX = numberOfCellsX;
        this.numberOfCellsY = numberOfCellsY;
        this.cellSize = cellSize;
        this.xCoordOfGridCellZero = xCoordOfGridCellZero;
        this.yCoordOfGridCellZero = yCoordOfGridCellZero;
        this.isInterval = isInterval;
        this.isTimeStamped = isTimeStamped;
        this.timeZoneId = Objects.requireNonNull(timeZoneId);
        this.srsName = Objects.requireNonNull(srsName);
        this.srsDefinition = Objects.requireNonNull(srsDefinition);
        this.srsDefinitionType = srsDefinitionType;
        this.maxDataValue = maxDataValue;
        this.minDataValue = minDataValue;
        this.meanDataValue = meanDataValue;
        this.rangeHistogram = Objects.requireNonNull(rangeHistogram);
    }

    /**
     * Creates a grid with sensible defaults. Statistics are computed from the data.
     */
    public static DssGrid of(double[] data, int cellsX, int cellsY,
                             double cellSize, double xOrigin, double yOrigin) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0;
        int count = 0;
        for (double v : data) {
            if (!Double.isNaN(v)) {
                min = Math.min(min, v);
                max = Math.max(max, v);
                sum += v;
                count++;
            }
        }
        double mean = count > 0 ? sum / count : 0;
        if (count == 0) { min = 0; max = 0; }

        return new DssGrid(data,
                GridType.ALBERS, GridDataType.PER_AVER, 0, 0, cellsX, cellsY,
                cellSize, xOrigin, yOrigin, false, false, "",
                "", "", 0,
                max, min, mean,
                RangeHistogram.EMPTY);
    }

    /**
     * Returns a new grid with spatial reference set.
     */
    public DssGrid withSrs(String name, String wktDefinition) {
        return new DssGrid(data, gridType, dataType,
                lowerLeftCellX, lowerLeftCellY, numberOfCellsX, numberOfCellsY,
                cellSize, xCoordOfGridCellZero, yCoordOfGridCellZero, isInterval, isTimeStamped,
                timeZoneId,
                name, wktDefinition, 0,
                maxDataValue, minDataValue, meanDataValue,
                rangeHistogram);
    }

    /**
     * Returns a new grid with time zone set.
     */
    public DssGrid withTimeZone(String timeZoneId) {
        return new DssGrid(data, gridType, dataType,
                lowerLeftCellX, lowerLeftCellY, numberOfCellsX, numberOfCellsY,
                cellSize, xCoordOfGridCellZero, yCoordOfGridCellZero, isInterval, isTimeStamped,
                timeZoneId,
                srsName, srsDefinition, srsDefinitionType,
                maxDataValue, minDataValue, meanDataValue,
                rangeHistogram);
    }

    // Data
    public double[] data() { return data.clone(); }

    // Grid info
    public GridType gridType() { return gridType; }
    public GridDataType dataType() { return dataType; }
    public int lowerLeftCellX() { return lowerLeftCellX; }
    public int lowerLeftCellY() { return lowerLeftCellY; }
    public int numberOfCellsX() { return numberOfCellsX; }
    public int numberOfCellsY() { return numberOfCellsY; }
    public double cellSize() { return cellSize; }
    public double xCoordOfGridCellZero() { return xCoordOfGridCellZero; }
    public double yCoordOfGridCellZero() { return yCoordOfGridCellZero; }
    public boolean isInterval() { return isInterval; }
    public boolean isTimeStamped() { return isTimeStamped; }
    public String timeZoneId() { return timeZoneId; }

    // Spatial reference
    public String srsName() { return srsName; }
    public String srsDefinition() { return srsDefinition; }
    public int srsDefinitionType() { return srsDefinitionType; }

    // Statistics
    public double maxDataValue() { return maxDataValue; }
    public double minDataValue() { return minDataValue; }
    public double meanDataValue() { return meanDataValue; }
    public RangeHistogram rangeHistogram() { return rangeHistogram; }
}

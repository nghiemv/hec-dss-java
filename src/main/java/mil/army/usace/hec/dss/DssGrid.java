package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Gridded (raster) data read from or written to a DSS file.
 * Contains the cell data, grid dimensions, spatial reference, and statistics.
 */
public final class DssGrid {
    // Data
    private final float[] data;

    // Grid info
    private final int gridType;
    private final int dataType;
    private final int lowerLeftCellX;
    private final int lowerLeftCellY;
    private final int numberOfCellsX;
    private final int numberOfCellsY;
    private final int numberOfRanges;
    private final float cellSize;
    private final float xCoordOfGridCellZero;
    private final float yCoordOfGridCellZero;
    private final boolean isInterval;
    private final boolean isTimeStamped;
    private final String timeZoneId;
    private final int timeZoneRawOffset;

    // Spatial reference
    private final String srsName;
    private final String srsDefinition;
    private final int srsDefinitionType;

    // Statistics
    private final float nullValue;
    private final float maxDataValue;
    private final float minDataValue;
    private final float meanDataValue;
    private final float[] rangeLimitTable;
    private final int[] numberEqualOrExceedingRangeLimit;

    public DssGrid(float[] data,
                   int gridType, int dataType,
                   int lowerLeftCellX, int lowerLeftCellY,
                   int numberOfCellsX, int numberOfCellsY,
                   int numberOfRanges,
                   float cellSize, float xCoordOfGridCellZero, float yCoordOfGridCellZero,
                   boolean isInterval, boolean isTimeStamped,
                   String timeZoneId, int timeZoneRawOffset,
                   String srsName, String srsDefinition, int srsDefinitionType,
                   float nullValue, float maxDataValue, float minDataValue, float meanDataValue,
                   float[] rangeLimitTable, int[] numberEqualOrExceedingRangeLimit) {
        this.data = Objects.requireNonNull(data).clone();
        this.gridType = gridType;
        this.dataType = dataType;
        this.lowerLeftCellX = lowerLeftCellX;
        this.lowerLeftCellY = lowerLeftCellY;
        this.numberOfCellsX = numberOfCellsX;
        this.numberOfCellsY = numberOfCellsY;
        this.numberOfRanges = numberOfRanges;
        this.cellSize = cellSize;
        this.xCoordOfGridCellZero = xCoordOfGridCellZero;
        this.yCoordOfGridCellZero = yCoordOfGridCellZero;
        this.isInterval = isInterval;
        this.isTimeStamped = isTimeStamped;
        this.timeZoneId = Objects.requireNonNull(timeZoneId);
        this.timeZoneRawOffset = timeZoneRawOffset;
        this.srsName = Objects.requireNonNull(srsName);
        this.srsDefinition = Objects.requireNonNull(srsDefinition);
        this.srsDefinitionType = srsDefinitionType;
        this.nullValue = nullValue;
        this.maxDataValue = maxDataValue;
        this.minDataValue = minDataValue;
        this.meanDataValue = meanDataValue;
        this.rangeLimitTable = Objects.requireNonNull(rangeLimitTable).clone();
        this.numberEqualOrExceedingRangeLimit = Objects.requireNonNull(numberEqualOrExceedingRangeLimit).clone();
    }

    // Data
    public float[] data() { return data.clone(); }

    // Grid info
    public int gridType() { return gridType; }
    public int dataType() { return dataType; }
    public int lowerLeftCellX() { return lowerLeftCellX; }
    public int lowerLeftCellY() { return lowerLeftCellY; }
    public int numberOfCellsX() { return numberOfCellsX; }
    public int numberOfCellsY() { return numberOfCellsY; }
    public int numberOfRanges() { return numberOfRanges; }
    public float cellSize() { return cellSize; }
    public float xCoordOfGridCellZero() { return xCoordOfGridCellZero; }
    public float yCoordOfGridCellZero() { return yCoordOfGridCellZero; }
    public boolean isInterval() { return isInterval; }
    public boolean isTimeStamped() { return isTimeStamped; }
    public String timeZoneId() { return timeZoneId; }
    public int timeZoneRawOffset() { return timeZoneRawOffset; }

    // Spatial reference
    public String srsName() { return srsName; }
    public String srsDefinition() { return srsDefinition; }
    public int srsDefinitionType() { return srsDefinitionType; }

    // Statistics
    public float nullValue() { return nullValue; }
    public float maxDataValue() { return maxDataValue; }
    public float minDataValue() { return minDataValue; }
    public float meanDataValue() { return meanDataValue; }
    public float[] rangeLimitTable() { return rangeLimitTable.clone(); }
    public int[] numberEqualOrExceedingRangeLimit() { return numberEqualOrExceedingRangeLimit.clone(); }
}

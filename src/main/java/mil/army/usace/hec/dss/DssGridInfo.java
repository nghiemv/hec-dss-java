package mil.army.usace.hec.dss;

/**
 * Grid dimensions, cell layout, and temporal metadata.
 */
public record DssGridInfo(
        int gridType, int dataType,
        int lowerLeftCellX, int lowerLeftCellY,
        int numberOfCellsX, int numberOfCellsY,
        int numberOfRanges,
        float cellSize,
        float xCoordOfGridCellZero, float yCoordOfGridCellZero,
        boolean isInterval, boolean isTimeStamped,
        String timeZoneId, int timeZoneRawOffset
) { }

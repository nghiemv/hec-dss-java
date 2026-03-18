package mil.army.usace.hec.dss.internal;

/**
 * DSS-native grid metadata preserved for round-trip fidelity.
 * Not exposed in the public API.
 */
public record NativeGridMetadata(
        int gridTypeCode,
        int lowerLeftCellX, int lowerLeftCellY,
        float xCoordOfGridCellZero, float yCoordOfGridCellZero,
        int srsDefinitionType,
        String srsName, String srsDefinition,
        boolean isInterval, boolean isTimeStamped,
        String timeZoneId, String dataSource,
        float maxDataValue, float minDataValue, float meanDataValue,
        RangeHistogram rangeHistogram
) {}

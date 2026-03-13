package mil.army.usace.hec.dss;

import java.util.Objects;

/**
 * Summary statistics and range tables for a grid record.
 */
public record DssGridStatistics(
        float nullValue,
        float maxDataValue, float minDataValue, float meanDataValue,
        float[] rangeLimitTable, int[] numberEqualOrExceedingRangeLimit
) {
    public DssGridStatistics {
        Objects.requireNonNull(rangeLimitTable);
        Objects.requireNonNull(numberEqualOrExceedingRangeLimit);
    }
}

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
        rangeLimitTable = Objects.requireNonNull(rangeLimitTable).clone();
        numberEqualOrExceedingRangeLimit = Objects.requireNonNull(numberEqualOrExceedingRangeLimit).clone();
    }

    @Override public float[] rangeLimitTable() { return rangeLimitTable.clone(); }
    @Override public int[] numberEqualOrExceedingRangeLimit() { return numberEqualOrExceedingRangeLimit.clone(); }
}

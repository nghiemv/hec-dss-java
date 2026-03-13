package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssTimeSeries;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Zero-copy DssTimeSeries backed directly by native MemorySegments.
 * No Java heap arrays are allocated — values are read on demand from off-heap memory
 * owned by the parent DssSession's Arena.
 */
final class NativeTimeSeries implements DssTimeSeries {
    private static final long BASE_EPOCH_SECONDS =
            LocalDateTime.of(1899, 12, 31, 0, 0).toEpochSecond(ZoneOffset.UTC);

    private final MemorySegment timeSegment;
    private final MemorySegment valueSegment;
    private final int count;
    private final int timeGranularitySeconds;
    private final String dataUnits;
    private final String dataType;

    NativeTimeSeries(MemorySegment timeSegment, MemorySegment valueSegment,
                     int count, int timeGranularitySeconds,
                     String dataUnits, String dataType) {
        this.timeSegment = timeSegment;
        this.valueSegment = valueSegment;
        this.count = count;
        this.timeGranularitySeconds = timeGranularitySeconds;
        this.dataUnits = dataUnits;
        this.dataType = dataType;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public double value(int index) {
        return valueSegment.getAtIndex(ValueLayout.JAVA_DOUBLE, index);
    }

    @Override
    public Instant time(int index) {
        int timeDelta = timeSegment.getAtIndex(ValueLayout.JAVA_INT, index);
        long epochSeconds = BASE_EPOCH_SECONDS + (long) timeDelta * timeGranularitySeconds;
        return Instant.ofEpochSecond(epochSeconds);
    }

    @Override
    public String dataUnits() {
        return dataUnits;
    }

    @Override
    public String dataType() {
        return dataType;
    }
}

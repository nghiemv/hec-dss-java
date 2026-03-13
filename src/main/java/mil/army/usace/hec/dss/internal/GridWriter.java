package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class GridWriter {
    private static final float NULL_SENTINEL = -Float.MAX_VALUE;

    private GridWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssGrid grid) {
        Arena arena = session.arena();

        // Narrow double→float and convert NaN→sentinel
        double[] doubleData = grid.data();
        float[] nativeData = new float[doubleData.length];
        for (int i = 0; i < doubleData.length; i++) {
            nativeData[i] = Double.isNaN(doubleData[i]) ? NULL_SENTINEL : (float) doubleData[i];
        }

        DssGrid.RangeHistogram histogram = grid.rangeHistogram();
        double[] doubleLimits = histogram.limits();
        float[] nativeRangeTable = new float[doubleLimits.length];
        for (int i = 0; i < doubleLimits.length; i++) {
            nativeRangeTable[i] = (float) doubleLimits[i];
        }

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment dataUnitsInput = arena.allocateFrom("");
        MemorySegment dataSourceInput = arena.allocateFrom("");
        MemorySegment srsNameInput = arena.allocateFrom(grid.srsName());
        MemorySegment srsDefinitionInput = arena.allocateFrom(grid.srsDefinition());
        MemorySegment timeZoneIdInput = arena.allocateFrom(grid.timeZoneId());

        MemorySegment rangeLimitInput = NativeBuffers.allocateFloats(arena, nativeRangeTable);
        MemorySegment rangeExceedInput = NativeBuffers.allocateInts(arena, histogram.exceedanceCounts());
        MemorySegment dataInput = NativeBuffers.allocateFloats(arena, nativeData);

        int status = hecdss_h.hec_dss_gridStore(
                session.dssPointer(), pathnameInput,
                grid.gridType().code(), grid.dataType().code(),
                grid.lowerLeftCellX(), grid.lowerLeftCellY(),
                grid.numberOfCellsX(), grid.numberOfCellsY(),
                histogram.size(),
                grid.srsDefinitionType(),
                0, // timeZoneRawOffset — computed by native library
                grid.isInterval() ? 1 : 0,
                grid.isTimeStamped() ? 1 : 0,
                0, // compressionSize
                dataUnitsInput, dataSourceInput,
                srsNameInput, srsDefinitionInput, timeZoneIdInput,
                (float) grid.cellSize(),
                (float) grid.xCoordOfGridCellZero(), (float) grid.yCoordOfGridCellZero(),
                NULL_SENTINEL,
                (float) grid.maxDataValue(), (float) grid.minDataValue(), (float) grid.meanDataValue(),
                rangeLimitInput, rangeExceedInput,
                dataInput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write grid '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }
}

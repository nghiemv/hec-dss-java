package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class GridWriter {
    private GridWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssGrid grid) {
        Arena arena = session.arena();
        DssGridInfo info = grid.info();
        DssGridSpatialReference srs = grid.srs();
        DssGridStatistics stats = grid.stats();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment dataUnitsInput = arena.allocateFrom("");
        MemorySegment dataSourceInput = arena.allocateFrom("");
        MemorySegment srsNameInput = arena.allocateFrom(srs.name());
        MemorySegment srsDefinitionInput = arena.allocateFrom(srs.definition());
        MemorySegment timeZoneIdInput = arena.allocateFrom(info.timeZoneId());

        MemorySegment rangeLimitInput = NativeBuffers.allocateFloats(arena, stats.rangeLimitTable());
        MemorySegment rangeExceedInput = NativeBuffers.allocateInts(arena, stats.numberEqualOrExceedingRangeLimit());
        MemorySegment dataInput = NativeBuffers.allocateFloats(arena, grid.data());

        int status = hecdss_h.hec_dss_gridStore(
                session.dssPointer(), pathnameInput,
                info.gridType(), info.dataType(),
                info.lowerLeftCellX(), info.lowerLeftCellY(),
                info.numberOfCellsX(), info.numberOfCellsY(),
                info.numberOfRanges(),
                srs.definitionType(),
                info.timeZoneRawOffset(),
                info.isInterval() ? 1 : 0,
                info.isTimeStamped() ? 1 : 0,
                0, // compressionSize
                dataUnitsInput, dataSourceInput,
                srsNameInput, srsDefinitionInput, timeZoneIdInput,
                info.cellSize(),
                info.xCoordOfGridCellZero(), info.yCoordOfGridCellZero(),
                stats.nullValue(),
                stats.maxDataValue(), stats.minDataValue(), stats.meanDataValue(),
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

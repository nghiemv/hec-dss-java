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
        NativeGridMetadata meta = grid.nativeMetadata();

        int width = grid.width();
        int height = grid.height();
        float[] data = grid.data();

        // Flip: row 0 (north) → last native row (bottom-to-top), replace NaN with sentinel
        float[] nativeData = new float[width * height];
        for (int row = 0; row < height; row++) {
            int dstRow = height - 1 - row;
            for (int col = 0; col < width; col++) {
                float v = data[row * width + col];
                nativeData[dstRow * width + col] = Float.isNaN(v) ? NULL_SENTINEL : v;
            }
        }

        // Resolve native fields — round-trip uses stored metadata, user-constructed derives them
        int gridTypeCode;
        int lowerLeftCellX, lowerLeftCellY;
        float nativeXOrigin, nativeYOrigin;
        int srsDefinitionType;
        String srsName, srsDefinition, timeZoneId, dataSource;
        boolean isInterval, isTimeStamped;
        float maxVal, minVal, meanVal;
        float[] nativeRangeTable;
        int[] rangeExceedance;
        int numRanges;

        if (meta != null) {
            // Round-trip: use stored native metadata
            gridTypeCode = meta.gridTypeCode();
            lowerLeftCellX = meta.lowerLeftCellX();
            lowerLeftCellY = meta.lowerLeftCellY();
            nativeXOrigin = meta.xCoordOfGridCellZero();
            nativeYOrigin = meta.yCoordOfGridCellZero();
            srsDefinitionType = meta.srsDefinitionType();
            srsName = meta.srsName();
            srsDefinition = meta.srsDefinition();
            isInterval = meta.isInterval();
            isTimeStamped = meta.isTimeStamped();
            timeZoneId = meta.timeZoneId();
            dataSource = meta.dataSource();
            maxVal = meta.maxDataValue();
            minVal = meta.minDataValue();
            meanVal = meta.meanDataValue();

            RangeHistogram histogram = meta.rangeHistogram();
            nativeRangeTable = histogram.limits();
            rangeExceedance = histogram.exceedanceCounts();
            numRanges = histogram.size();
        } else {
            // User-constructed: derive native fields from grid geometry
            lowerLeftCellX = 0;
            lowerLeftCellY = 0;
            nativeXOrigin = grid.xOrigin();
            nativeYOrigin = grid.yOrigin();

            GridType gridType = GridType.fromCrs(grid.crs(), false);
            gridTypeCode = gridType.code();
            srsDefinitionType = 0;
            srsName = "";
            srsDefinition = "";
            isInterval = false;
            isTimeStamped = false;
            timeZoneId = "";
            dataSource = "";

            // Compute statistics from data
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;
            double sum = 0;
            int count = 0;
            for (float v : nativeData) {
                if (v != NULL_SENTINEL) {
                    min = Math.min(min, v);
                    max = Math.max(max, v);
                    sum += v;
                    count++;
                }
            }
            maxVal = count > 0 ? max : 0;
            minVal = count > 0 ? min : 0;
            meanVal = count > 0 ? (float) (sum / count) : 0;

            nativeRangeTable = new float[0];
            rangeExceedance = new int[0];
            numRanges = 0;
        }

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment dataUnitsInput = arena.allocateFrom(grid.units());
        MemorySegment dataSourceInput = arena.allocateFrom(dataSource);
        MemorySegment srsNameInput = arena.allocateFrom(srsName);
        MemorySegment srsDefinitionInput = arena.allocateFrom(srsDefinition);
        MemorySegment timeZoneIdInput = arena.allocateFrom(timeZoneId);

        MemorySegment rangeLimitInput = NativeBuffers.allocateFloats(arena, nativeRangeTable);
        MemorySegment rangeExceedInput = NativeBuffers.allocateInts(arena, rangeExceedance);
        MemorySegment dataInput = NativeBuffers.allocateFloats(arena, nativeData);

        int status = hecdss_h.hec_dss_gridStore(
                session.dssPointer(), pathnameInput,
                gridTypeCode, grid.dataType().code(),
                lowerLeftCellX, lowerLeftCellY,
                width, height,
                numRanges,
                srsDefinitionType,
                0, // timeZoneRawOffset — computed by native library
                isInterval ? 1 : 0,
                isTimeStamped ? 1 : 0,
                0, // compressionSize
                dataUnitsInput, dataSourceInput,
                srsNameInput, srsDefinitionInput, timeZoneIdInput,
                grid.cellSize(),
                nativeXOrigin, nativeYOrigin,
                NULL_SENTINEL,
                maxVal, minVal, meanVal,
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

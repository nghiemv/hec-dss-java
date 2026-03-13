package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class GridReader {
    private static final int STRING_BUFFER_LENGTH = 100;
    private static final int SRS_DEFINITION_BUFFER_LENGTH = 10000;
    private static final int MAX_RANGES = 100;

    private GridReader() {}

    public static DssGrid read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        // First retrieve metadata only (boolRetrieveData = 0) to get grid dimensions
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment typeOutput = arena.allocate(C_INT, 1);
        MemorySegment dataTypeOutput = arena.allocate(C_INT, 1);
        MemorySegment lowerLeftCellXOutput = arena.allocate(C_INT, 1);
        MemorySegment lowerLeftCellYOutput = arena.allocate(C_INT, 1);
        MemorySegment numberOfCellsXOutput = arena.allocate(C_INT, 1);
        MemorySegment numberOfCellsYOutput = arena.allocate(C_INT, 1);
        MemorySegment numberOfRangesOutput = arena.allocate(C_INT, 1);
        MemorySegment srsDefinitionTypeOutput = arena.allocate(C_INT, 1);
        MemorySegment timeZoneRawOffsetOutput = arena.allocate(C_INT, 1);
        MemorySegment isIntervalOutput = arena.allocate(C_INT, 1);
        MemorySegment isTimeStampedOutput = arena.allocate(C_INT, 1);
        MemorySegment dataUnitsOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment dataSourceOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment srsNameOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment srsDefinitionOutput = arena.allocate(C_CHAR, SRS_DEFINITION_BUFFER_LENGTH);
        MemorySegment timeZoneIDOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment cellSizeOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment xCoordOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment yCoordOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment nullValueOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment maxDataValueOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment minDataValueOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment meanDataValueOutput = arena.allocate(C_FLOAT, 1);
        MemorySegment rangeLimitTableOutput = arena.allocate(C_FLOAT, MAX_RANGES);
        MemorySegment rangeExceedanceOutput = arena.allocate(C_INT, MAX_RANGES);
        MemorySegment dataOutput = arena.allocate(C_FLOAT, 1); // placeholder for metadata-only call

        int status = hecdss_h.hec_dss_gridRetrieve(
                session.dssPointer(), pathnameInput, 0,
                typeOutput, dataTypeOutput,
                lowerLeftCellXOutput, lowerLeftCellYOutput,
                numberOfCellsXOutput, numberOfCellsYOutput,
                numberOfRangesOutput, srsDefinitionTypeOutput,
                timeZoneRawOffsetOutput, isIntervalOutput, isTimeStampedOutput,
                dataUnitsOutput, STRING_BUFFER_LENGTH,
                dataSourceOutput, STRING_BUFFER_LENGTH,
                srsNameOutput, STRING_BUFFER_LENGTH,
                srsDefinitionOutput, SRS_DEFINITION_BUFFER_LENGTH,
                timeZoneIDOutput, STRING_BUFFER_LENGTH,
                cellSizeOutput, xCoordOutput, yCoordOutput,
                nullValueOutput, maxDataValueOutput, minDataValueOutput, meanDataValueOutput,
                rangeLimitTableOutput, MAX_RANGES,
                rangeExceedanceOutput,
                dataOutput, 0
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get grid info for '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        int cellsX = numberOfCellsXOutput.get(C_INT, 0);
        int cellsY = numberOfCellsYOutput.get(C_INT, 0);
        int dataLength = cellsX * cellsY;
        int numRanges = numberOfRangesOutput.get(C_INT, 0);

        // Now retrieve with data
        MemorySegment pathnameInput2 = arena.allocateFrom(pathname.toString());
        MemorySegment typeOutput2 = arena.allocate(C_INT, 1);
        MemorySegment dataTypeOutput2 = arena.allocate(C_INT, 1);
        MemorySegment lowerLeftCellXOutput2 = arena.allocate(C_INT, 1);
        MemorySegment lowerLeftCellYOutput2 = arena.allocate(C_INT, 1);
        MemorySegment numberOfCellsXOutput2 = arena.allocate(C_INT, 1);
        MemorySegment numberOfCellsYOutput2 = arena.allocate(C_INT, 1);
        MemorySegment numberOfRangesOutput2 = arena.allocate(C_INT, 1);
        MemorySegment srsDefinitionTypeOutput2 = arena.allocate(C_INT, 1);
        MemorySegment timeZoneRawOffsetOutput2 = arena.allocate(C_INT, 1);
        MemorySegment isIntervalOutput2 = arena.allocate(C_INT, 1);
        MemorySegment isTimeStampedOutput2 = arena.allocate(C_INT, 1);
        MemorySegment dataUnitsOutput2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment dataSourceOutput2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment srsNameOutput2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment srsDefinitionOutput2 = arena.allocate(C_CHAR, SRS_DEFINITION_BUFFER_LENGTH);
        MemorySegment timeZoneIDOutput2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment cellSizeOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment xCoordOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment yCoordOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment nullValueOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment maxDataValueOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment minDataValueOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment meanDataValueOutput2 = arena.allocate(C_FLOAT, 1);
        MemorySegment rangeLimitTableOutput2 = arena.allocate(C_FLOAT, Math.max(numRanges, 1));
        MemorySegment rangeExceedanceOutput2 = arena.allocate(C_INT, Math.max(numRanges, 1));
        MemorySegment fullDataOutput = arena.allocate(C_FLOAT, Math.max(dataLength, 1));

        status = hecdss_h.hec_dss_gridRetrieve(
                session.dssPointer(), pathnameInput2, 1,
                typeOutput2, dataTypeOutput2,
                lowerLeftCellXOutput2, lowerLeftCellYOutput2,
                numberOfCellsXOutput2, numberOfCellsYOutput2,
                numberOfRangesOutput2, srsDefinitionTypeOutput2,
                timeZoneRawOffsetOutput2, isIntervalOutput2, isTimeStampedOutput2,
                dataUnitsOutput2, STRING_BUFFER_LENGTH,
                dataSourceOutput2, STRING_BUFFER_LENGTH,
                srsNameOutput2, STRING_BUFFER_LENGTH,
                srsDefinitionOutput2, SRS_DEFINITION_BUFFER_LENGTH,
                timeZoneIDOutput2, STRING_BUFFER_LENGTH,
                cellSizeOutput2, xCoordOutput2, yCoordOutput2,
                nullValueOutput2, maxDataValueOutput2, minDataValueOutput2, meanDataValueOutput2,
                rangeLimitTableOutput2, numRanges,
                rangeExceedanceOutput2,
                fullDataOutput, dataLength
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve grid data '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        float[] data = dataLength > 0
                ? fullDataOutput.asSlice(0, (long) dataLength * ValueLayout.JAVA_FLOAT.byteSize())
                    .toArray(ValueLayout.JAVA_FLOAT)
                : new float[0];

        float[] rangeTable = numRanges > 0
                ? rangeLimitTableOutput2.asSlice(0, (long) numRanges * ValueLayout.JAVA_FLOAT.byteSize())
                    .toArray(ValueLayout.JAVA_FLOAT)
                : new float[0];
        int[] rangeExceedance = numRanges > 0
                ? rangeExceedanceOutput2.asSlice(0, (long) numRanges * ValueLayout.JAVA_INT.byteSize())
                    .toArray(ValueLayout.JAVA_INT)
                : new int[0];

        DssGridInfo info = new DssGridInfo(
                typeOutput2.get(C_INT, 0),
                dataTypeOutput2.get(C_INT, 0),
                lowerLeftCellXOutput2.get(C_INT, 0),
                lowerLeftCellYOutput2.get(C_INT, 0),
                cellsX, cellsY,
                numRanges,
                cellSizeOutput2.get(C_FLOAT, 0),
                xCoordOutput2.get(C_FLOAT, 0),
                yCoordOutput2.get(C_FLOAT, 0),
                isIntervalOutput2.get(C_INT, 0) != 0,
                isTimeStampedOutput2.get(C_INT, 0) != 0,
                timeZoneIDOutput2.getString(0),
                timeZoneRawOffsetOutput2.get(C_INT, 0)
        );

        DssGridSpatialReference srs = new DssGridSpatialReference(
                srsNameOutput2.getString(0),
                srsDefinitionOutput2.getString(0),
                srsDefinitionTypeOutput2.get(C_INT, 0)
        );

        DssGridStatistics stats = new DssGridStatistics(
                nullValueOutput2.get(C_FLOAT, 0),
                maxDataValueOutput2.get(C_FLOAT, 0),
                minDataValueOutput2.get(C_FLOAT, 0),
                meanDataValueOutput2.get(C_FLOAT, 0),
                rangeTable, rangeExceedance
        );

        return new DssGrid(data, info, srs, stats);
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class GridReader {
    private static final int STRING_BUFFER_LENGTH = NativeBuffers.STRING_BUFFER_LENGTH;
    private static final int SRS_DEFINITION_BUFFER_LENGTH = 10000;
    private static final int MAX_RANGES = 100;

    private GridReader() {}

    public static DssGrid read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        // Allocate all output segments once — reused across both native calls
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

        // First call: metadata only (boolRetrieveData = 0) to get grid dimensions
        MemorySegment dataPlaceholder = arena.allocate(C_FLOAT, 1);

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
                dataPlaceholder, 0
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

        // Second call: retrieve with data, reusing all metadata segments
        MemorySegment rangeLimitSized = numRanges > 0 && numRanges <= MAX_RANGES
                ? rangeLimitTableOutput
                : arena.allocate(C_FLOAT, Math.max(numRanges, 1));
        MemorySegment rangeExceedSized = numRanges > 0 && numRanges <= MAX_RANGES
                ? rangeExceedanceOutput
                : arena.allocate(C_INT, Math.max(numRanges, 1));
        MemorySegment fullDataOutput = arena.allocate(C_FLOAT, Math.max(dataLength, 1));

        status = hecdss_h.hec_dss_gridRetrieve(
                session.dssPointer(), pathnameInput, 1,
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
                rangeLimitSized, numRanges,
                rangeExceedSized,
                fullDataOutput, dataLength
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve grid data '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        float[] nativeData = dataLength > 0
                ? fullDataOutput.asSlice(0, (long) dataLength * ValueLayout.JAVA_FLOAT.byteSize())
                    .toArray(ValueLayout.JAVA_FLOAT)
                : new float[0];

        float[] nativeRangeTable = numRanges > 0
                ? rangeLimitSized.asSlice(0, (long) numRanges * ValueLayout.JAVA_FLOAT.byteSize())
                    .toArray(ValueLayout.JAVA_FLOAT)
                : new float[0];
        int[] rangeExceedance = numRanges > 0
                ? rangeExceedSized.asSlice(0, (long) numRanges * ValueLayout.JAVA_INT.byteSize())
                    .toArray(ValueLayout.JAVA_INT)
                : new int[0];

        // Widen float→double and convert null sentinel → NaN
        float nullValue = nullValueOutput.get(C_FLOAT, 0);
        double[] data = new double[nativeData.length];
        for (int i = 0; i < nativeData.length; i++) {
            data[i] = (nativeData[i] == nullValue) ? Double.NaN : nativeData[i];
        }

        double[] rangeTable = new double[nativeRangeTable.length];
        for (int i = 0; i < nativeRangeTable.length; i++) {
            rangeTable[i] = nativeRangeTable[i];
        }

        return new DssGrid(data,
                GridType.fromCode(typeOutput.get(C_INT, 0)),
                GridDataType.fromCode(dataTypeOutput.get(C_INT, 0)),
                lowerLeftCellXOutput.get(C_INT, 0), lowerLeftCellYOutput.get(C_INT, 0),
                cellsX, cellsY,
                cellSizeOutput.get(C_FLOAT, 0),
                xCoordOutput.get(C_FLOAT, 0), yCoordOutput.get(C_FLOAT, 0),
                isIntervalOutput.get(C_INT, 0) != 0,
                isTimeStampedOutput.get(C_INT, 0) != 0,
                timeZoneIDOutput.getString(0),
                srsNameOutput.getString(0), srsDefinitionOutput.getString(0),
                srsDefinitionTypeOutput.get(C_INT, 0),
                maxDataValueOutput.get(C_FLOAT, 0), minDataValueOutput.get(C_FLOAT, 0),
                meanDataValueOutput.get(C_FLOAT, 0),
                new DssGrid.RangeHistogram(rangeTable, rangeExceedance)
        );
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPairedData;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class PairedDataReader {
    private static final int STRING_BUFFER_LENGTH = 100;
    private static final int LABELS_BUFFER_LENGTH = 2000;

    private PairedDataReader() {}

    public static DssPairedData read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        // First get sizes
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment numberOrdinatesOutput = arena.allocate(C_INT, 1);
        MemorySegment numberCurvesOutput = arena.allocate(C_INT, 1);
        MemorySegment xUnitsOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment yUnitsOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment xTypeOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment yTypeOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment labelsLengthOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_pdRetrieveInfo(
                session.dssPointer(), pathnameInput,
                numberOrdinatesOutput, numberCurvesOutput,
                xUnitsOutput, STRING_BUFFER_LENGTH,
                yUnitsOutput, STRING_BUFFER_LENGTH,
                xTypeOutput, STRING_BUFFER_LENGTH,
                yTypeOutput, STRING_BUFFER_LENGTH,
                labelsLengthOutput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get paired data info for '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        int numberOrdinates = numberOrdinatesOutput.get(C_INT, 0);
        int numberCurves = numberCurvesOutput.get(C_INT, 0);
        int labelsLength = labelsLengthOutput.get(C_INT, 0);

        // Now retrieve the data
        MemorySegment ordinatesOutput = arena.allocate(C_DOUBLE, numberOrdinates);
        int valuesSize = numberOrdinates * numberCurves;
        MemorySegment valuesOutput = arena.allocate(C_DOUBLE, valuesSize);
        MemorySegment numberOrdinatesRead = arena.allocate(C_INT, 1);
        MemorySegment numberCurvesRead = arena.allocate(C_INT, 1);
        MemorySegment xUnits2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment xType2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment yUnits2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        MemorySegment yType2 = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);
        int actualLabelsLen = Math.max(labelsLength, 1);
        MemorySegment labelsOutput = arena.allocate(C_CHAR, actualLabelsLen);
        MemorySegment timezoneOutput = arena.allocate(C_CHAR, STRING_BUFFER_LENGTH);

        // Re-allocate pathname since we need it again
        MemorySegment pathnameInput2 = arena.allocateFrom(pathname.toString());

        status = hecdss_h.hec_dss_pdRetrieve(
                session.dssPointer(), pathnameInput2,
                ordinatesOutput, numberOrdinates,
                valuesOutput, valuesSize,
                numberOrdinatesRead, numberCurvesRead,
                xUnits2, STRING_BUFFER_LENGTH,
                xType2, STRING_BUFFER_LENGTH,
                yUnits2, STRING_BUFFER_LENGTH,
                yType2, STRING_BUFFER_LENGTH,
                labelsOutput, actualLabelsLen,
                timezoneOutput, STRING_BUFFER_LENGTH
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve paired data '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        double[] ordinates = ordinatesOutput.asSlice(0,
                (long) numberOrdinates * ValueLayout.JAVA_DOUBLE.byteSize())
                .toArray(ValueLayout.JAVA_DOUBLE);
        double[] values = valuesOutput.asSlice(0,
                (long) valuesSize * ValueLayout.JAVA_DOUBLE.byteSize())
                .toArray(ValueLayout.JAVA_DOUBLE);
        String[] labels = parseLabels(labelsOutput, actualLabelsLen, numberCurves);
        String xUnits = xUnits2.getString(0);
        String yUnits = yUnits2.getString(0);
        String xType = xType2.getString(0);
        String yType = yType2.getString(0);

        return new DssPairedData(ordinates, values, numberCurves, labels,
                xUnits, yUnits, xType, yType);
    }

    private static String[] parseLabels(MemorySegment buffer, int length, int numberCurves) {
        if (length <= 1) return new String[0];
        // Labels are null-terminated strings packed sequentially
        byte[] bytes = buffer.asSlice(0, length).toArray(ValueLayout.JAVA_BYTE);
        String[] labels = new String[numberCurves];
        int labelIndex = 0;
        int start = 0;
        for (int i = 0; i < bytes.length && labelIndex < numberCurves; i++) {
            if (bytes[i] == 0) {
                labels[labelIndex++] = new String(bytes, start, i - start);
                start = i + 1;
            }
        }
        if (labelIndex < numberCurves && start < bytes.length) {
            labels[labelIndex] = new String(bytes, start, bytes.length - start);
        }
        return labels;
    }
}

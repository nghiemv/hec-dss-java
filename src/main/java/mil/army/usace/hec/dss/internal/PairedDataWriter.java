package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPairedData;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class PairedDataWriter {
    private PairedDataWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssPairedData data) {
        Arena arena = session.arena();

        double[] ordinates = data.ordinates();
        // Reconstruct column-major flat array for native call
        int numOrds = data.numberOrdinates();
        int numCurves = data.numberCurves();
        double[] flatValues = new double[numOrds * numCurves];
        for (int c = 0; c < numCurves; c++) {
            double[] curve = data.curve(c);
            for (int i = 0; i < numOrds; i++) {
                flatValues[i * numCurves + c] = curve[i];
            }
        }
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment ordinatesInput = NativeBuffers.allocateDoubles(arena, ordinates);
        MemorySegment valuesInput = NativeBuffers.allocateDoubles(arena, flatValues);
        MemorySegment xUnitsInput = arena.allocateFrom(data.xUnits());
        MemorySegment xTypeInput = arena.allocateFrom(data.xType());
        MemorySegment yUnitsInput = arena.allocateFrom(data.yUnits());
        MemorySegment yTypeInput = arena.allocateFrom(data.yType());
        MemorySegment timezoneInput = arena.allocateFrom("");

        // Pack labels as null-separated bytes
        byte[] labelBytes = packLabels(data.labels());
        MemorySegment labelsInput;
        if (labelBytes.length > 0) {
            labelsInput = arena.allocate(C_CHAR, labelBytes.length);
            MemorySegment.copy(labelBytes, 0, labelsInput,
                    java.lang.foreign.ValueLayout.JAVA_BYTE, 0, labelBytes.length);
        } else {
            labelsInput = arena.allocate(C_CHAR, 1);
        }

        int status = hecdss_h.hec_dss_pdStore(
                session.dssPointer(), pathnameInput,
                ordinatesInput, data.numberOrdinates(),
                valuesInput, flatValues.length,
                numOrds, numCurves,
                xUnitsInput, xTypeInput, yUnitsInput, yTypeInput,
                labelsInput, labelBytes.length,
                timezoneInput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write paired data '%s' to '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }
    }

    private static byte[] packLabels(String[] labels) {
        if (labels == null || labels.length == 0) return new byte[0];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.length; i++) {
            sb.append(labels[i] != null ? labels[i] : "");
            if (i < labels.length - 1) sb.append('\0');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

}

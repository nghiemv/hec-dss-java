package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class ArrayReader {
    private ArrayReader() {}

    public static double[] read(DssSession session, DssPathname pathname) {
        Arena arena = session.arena();

        // Get sizes first
        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());
        MemorySegment intCountOutput = arena.allocate(C_INT, 1);
        MemorySegment floatCountOutput = arena.allocate(C_INT, 1);
        MemorySegment doubleCountOutput = arena.allocate(C_INT, 1);

        int status = hecdss_h.hec_dss_arrayRetrieveInfo(
                session.dssPointer(), pathnameInput,
                intCountOutput, floatCountOutput, doubleCountOutput
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to get array info for '%s' from '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }

        int intCount = intCountOutput.get(C_INT, 0);
        int floatCount = floatCountOutput.get(C_INT, 0);
        int doubleCount = doubleCountOutput.get(C_INT, 0);

        // Retrieve data (reuse pathnameInput from above)
        MemorySegment intOutput = arena.allocate(C_INT, Math.max(intCount, 1));
        MemorySegment floatOutput = arena.allocate(C_FLOAT, Math.max(floatCount, 1));
        MemorySegment doubleOutput = arena.allocate(C_DOUBLE, Math.max(doubleCount, 1));

        status = hecdss_h.hec_dss_arrayRetrieve(
                session.dssPointer(), pathnameInput,
                intOutput, intCount,
                floatOutput, floatCount,
                doubleOutput, doubleCount
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to retrieve array '%s' from '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }

        // Widen all native types to double and concatenate
        double[] values = new double[intCount + floatCount + doubleCount];
        int offset = 0;
        for (int i = 0; i < intCount; i++) {
            values[offset++] = intOutput.getAtIndex(ValueLayout.JAVA_INT, i);
        }
        for (int i = 0; i < floatCount; i++) {
            values[offset++] = floatOutput.getAtIndex(ValueLayout.JAVA_FLOAT, i);
        }
        for (int i = 0; i < doubleCount; i++) {
            values[offset++] = doubleOutput.getAtIndex(ValueLayout.JAVA_DOUBLE, i);
        }

        return values;
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssArray;
import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class ArrayReader {
    private ArrayReader() {}

    public static DssArray read(DssSession session, DssPathname pathname) {
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
                    "Failed to get array info for '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
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
                    "Failed to retrieve array '%s' from '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }

        int[] intValues = intCount > 0
                ? intOutput.asSlice(0, (long) intCount * ValueLayout.JAVA_INT.byteSize())
                    .toArray(ValueLayout.JAVA_INT)
                : new int[0];
        float[] floatValues = floatCount > 0
                ? floatOutput.asSlice(0, (long) floatCount * ValueLayout.JAVA_FLOAT.byteSize())
                    .toArray(ValueLayout.JAVA_FLOAT)
                : new float[0];
        double[] doubleValues = doubleCount > 0
                ? doubleOutput.asSlice(0, (long) doubleCount * ValueLayout.JAVA_DOUBLE.byteSize())
                    .toArray(ValueLayout.JAVA_DOUBLE)
                : new double[0];

        return new DssArray(intValues, floatValues, doubleValues);
    }
}

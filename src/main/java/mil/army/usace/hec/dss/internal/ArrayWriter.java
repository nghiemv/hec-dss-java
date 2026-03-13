package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssArray;
import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class ArrayWriter {
    private ArrayWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssArray data) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());

        MemorySegment intInput = allocateInts(arena, data.intValues());
        MemorySegment floatInput = allocateFloats(arena, data.floatValues());
        MemorySegment doubleInput = allocateDoubles(arena, data.doubleValues());

        int status = hecdss_h.hec_dss_arrayStore(
                session.dssPointer(), pathnameInput,
                intInput, data.intValues().length,
                floatInput, data.floatValues().length,
                doubleInput, data.doubleValues().length
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write array '%s' to '%s': native status code %d"
                            .formatted(pathname, session.filePath(), status));
        }
    }

    private static MemorySegment allocateInts(Arena arena, int[] values) {
        if (values.length == 0) return arena.allocate(C_INT, 1);
        MemorySegment segment = arena.allocate(C_INT, values.length);
        MemorySegment.copy(values, 0, segment, C_INT, 0, values.length);
        return segment;
    }

    private static MemorySegment allocateFloats(Arena arena, float[] values) {
        if (values.length == 0) return arena.allocate(C_FLOAT, 1);
        MemorySegment segment = arena.allocate(C_FLOAT, values.length);
        MemorySegment.copy(values, 0, segment, C_FLOAT, 0, values.length);
        return segment;
    }

    private static MemorySegment allocateDoubles(Arena arena, double[] values) {
        if (values.length == 0) return arena.allocate(C_DOUBLE, 1);
        MemorySegment segment = arena.allocate(C_DOUBLE, values.length);
        MemorySegment.copy(values, 0, segment, C_DOUBLE, 0, values.length);
        return segment;
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class ArrayWriter {
    private ArrayWriter() {}

    public static void write(DssSession session, DssPathname pathname, double[] data) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());

        double[] doubles = data;
        MemorySegment intInput = arena.allocate(C_INT, 1);
        MemorySegment floatInput = arena.allocate(C_FLOAT, 1);
        MemorySegment doubleInput = NativeBuffers.allocateDoubles(arena, doubles);

        int status = hecdss_h.hec_dss_arrayStore(
                session.dssPointer(), pathnameInput,
                intInput, 0,
                floatInput, 0,
                doubleInput, doubles.length
        );

        if (status != 0) {
            throw new DssException(
                    "Failed to write array '%s' to '%s': %s"
                            .formatted(pathname, session.filePath(), NativeStatusCode.describe(status)));
        }
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssArray;
import mil.army.usace.hec.dss.DssException;
import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class ArrayWriter {
    private ArrayWriter() {}

    public static void write(DssSession session, DssPathname pathname, DssArray data) {
        Arena arena = session.arena();

        MemorySegment pathnameInput = arena.allocateFrom(pathname.toString());

        MemorySegment intInput = NativeBuffers.allocateInts(arena, data.intValues());
        MemorySegment floatInput = NativeBuffers.allocateFloats(arena, data.floatValues());
        MemorySegment doubleInput = NativeBuffers.allocateDoubles(arena, data.doubleValues());

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
}

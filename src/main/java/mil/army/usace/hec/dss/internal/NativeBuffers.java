package mil.army.usace.hec.dss.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

/**
 * Shared helpers for allocating native memory segments from Java arrays.
 */
final class NativeBuffers {
    static final int STRING_BUFFER_LENGTH = 100;

    private NativeBuffers() {}

    static MemorySegment allocateInts(Arena arena, int[] values) {
        if (values.length == 0) return arena.allocate(C_INT, 1);
        MemorySegment segment = arena.allocate(C_INT, values.length);
        MemorySegment.copy(values, 0, segment, C_INT, 0, values.length);
        return segment;
    }

    static MemorySegment allocateFloats(Arena arena, float[] values) {
        if (values.length == 0) return arena.allocate(C_FLOAT, 1);
        MemorySegment segment = arena.allocate(C_FLOAT, values.length);
        MemorySegment.copy(values, 0, segment, C_FLOAT, 0, values.length);
        return segment;
    }

    static MemorySegment allocateDoubles(Arena arena, double[] values) {
        if (values.length == 0) return arena.allocate(C_DOUBLE, 1);
        MemorySegment segment = arena.allocate(C_DOUBLE, values.length);
        MemorySegment.copy(values, 0, segment, C_DOUBLE, 0, values.length);
        return segment;
    }
}

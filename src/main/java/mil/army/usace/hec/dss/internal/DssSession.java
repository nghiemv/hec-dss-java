package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.logging.Logger;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class DssSession implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(DssSession.class.getName());

    private final String filePath;
    private final Arena arena;
    private final MemorySegment dssPointer;
    private boolean closed;

    private DssSession(String filePath, Arena arena, MemorySegment dssPointer) {
        this.filePath = filePath;
        this.arena = arena;
        this.dssPointer = dssPointer;
    }

    public static DssSession open(String filePath) throws DssException {
        NativeLibrary.load();

        Arena arena = Arena.ofConfined();
        try {
            MemorySegment pathHolder = arena.allocateFrom(filePath);
            MemorySegment pointerHolder = arena.allocate(C_POINTER);
            int status = hecdss_h.hec_dss_open(pathHolder, pointerHolder);

            if (status != 0) {
                throw new DssException("Failed to open DSS file '%s': status=%d"
                        .formatted(filePath, status));
            }

            MemorySegment dssPointer = pointerHolder.get(ValueLayout.ADDRESS, 0);
            return new DssSession(filePath, arena, dssPointer);
        } catch (DssException e) {
            arena.close();
            throw e;
        } catch (Exception e) {
            arena.close();
            throw new DssException("Failed to open DSS file '%s'".formatted(filePath), e);
        }
    }

    Arena arena() {
        return arena;
    }

    MemorySegment dssPointer() {
        return dssPointer;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;

        try {
            int status = hecdss_h.hec_dss_close(dssPointer);
            if (status != 0) {
                logger.severe("Failed to close DSS file '%s': status=%d"
                        .formatted(filePath, status));
            }
        } finally {
            arena.close();
        }
    }
}

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.file.Path;
import java.util.logging.Logger;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class DssSession implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(DssSession.class.getName());

    private final Path filePath;
    private final Arena arena;
    private final MemorySegment dssPointer;
    private boolean closed;

    private DssSession(Path filePath, Arena arena, MemorySegment dssPointer) {
        this.filePath = filePath;
        this.arena = arena;
        this.dssPointer = dssPointer;
    }

    public static DssSession open(Path filePath) {
        NativeLibrary.load();

        String pathString = filePath.toAbsolutePath().toString();
        Arena arena = Arena.ofConfined();
        try {
            MemorySegment pathHolder = arena.allocateFrom(pathString);
            MemorySegment pointerHolder = arena.allocate(C_POINTER);
            int status = hecdss_h.hec_dss_open(pathHolder, pointerHolder);

            if (status != 0) {
                throw new DssException(
                        "Cannot open DSS file '%s': native status code %d".formatted(filePath, status));
            }

            MemorySegment dssPointer = pointerHolder.get(ValueLayout.ADDRESS, 0);
            return new DssSession(filePath, arena, dssPointer);
        } catch (DssException e) {
            arena.close();
            throw e;
        } catch (Exception e) {
            arena.close();
            throw new DssException(
                    "Cannot open DSS file '%s'".formatted(filePath), e);
        }
    }

    Path filePath() {
        return filePath;
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

package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

final class DssCatalogService {
    private static final int MAX_PATHNAME_LENGTH = 1000;

    private final DssSession dssSession;

    DssCatalogService(DssSession dssSession) {
        this.dssSession = dssSession;
    }

    Stream<DssPathname> getCatalog() {
        DssPathname matchAllPattern = DssPathname.matchAllPattern();
        return getCatalog(matchAllPattern);
    }

    Stream<DssPathname> getCatalog(DssPathname filterPattern) {
        Arena arena = dssSession.getMemorySession();

        int recordCount = getRecordCount();
        int charBufferLength = MAX_PATHNAME_LENGTH * recordCount;

        MemorySegment pathBuffer = arena.allocate(C_CHAR, charBufferLength);
        MemorySegment recordTypes = arena.allocate(C_INT, recordCount);
        MemorySegment pathFilter = arena.allocateFrom(filterPattern.toString());

        hecdss_h.hec_dss_catalog(
                dssSession.getDssStackPointer(),
                pathBuffer,
                recordTypes,
                pathFilter,
                recordCount,
                MAX_PATHNAME_LENGTH
        );

        return parseNullTerminatedStrings(pathBuffer).stream()
                .map(DssPathname::parse)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    int getRecordCount() {
        return hecdss_h.hec_dss_record_count(dssSession.getDssStackPointer());
    }

    private static java.util.List<String> parseNullTerminatedStrings(MemorySegment buffer) {
        String bufferContent = StandardCharsets.ISO_8859_1.decode(buffer.asByteBuffer()).toString();
        return Arrays.stream(bufferContent.split("\0"))
                .filter(s -> !s.isBlank())
                .toList();
    }
}

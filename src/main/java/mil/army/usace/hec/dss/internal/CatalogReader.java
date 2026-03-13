package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class CatalogReader {
    private static final int MAX_PATHNAME_LENGTH = 1000;

    private CatalogReader() {}

    public static List<DssPathname> read(DssSession session) {
        return read(session, new DssPathname("*", "*", "*", "*", "*", "*"));
    }

    public static List<DssPathname> read(DssSession session, DssPathname filter) {
        Arena arena = session.arena();

        int count = recordCount(session);
        long bufferLength = Math.multiplyExact((long) MAX_PATHNAME_LENGTH, count);

        MemorySegment pathBuffer = arena.allocate(C_CHAR, bufferLength);
        MemorySegment recordTypes = arena.allocate(C_INT, count);
        MemorySegment pathFilter = arena.allocateFrom(filter.toString());

        // hec_dss_catalog returns the number of records read, not a status code
        hecdss_h.hec_dss_catalog(
                session.dssPointer(), pathBuffer, recordTypes,
                pathFilter, count, MAX_PATHNAME_LENGTH
        );

        return parseNullTerminatedStrings(pathBuffer, bufferLength).stream()
                .map(DssPathname::parse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public static int recordCount(DssSession session) {
        return hecdss_h.hec_dss_record_count(session.dssPointer());
    }

    private static List<String> parseNullTerminatedStrings(MemorySegment buffer, long length) {
        List<String> result = new ArrayList<>();
        long offset = 0;
        while (offset < length) {
            byte b = buffer.get(C_CHAR, offset);
            if (b == 0) {
                offset++;
                continue;
            }
            String s = buffer.getString(offset);
            if (!s.isBlank()) {
                result.add(s);
            }
            offset += s.length() + 1; // skip past null terminator
        }
        return result;
    }
}

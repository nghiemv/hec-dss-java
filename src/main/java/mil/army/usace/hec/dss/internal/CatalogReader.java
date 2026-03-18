package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssCatalogEntry;
import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.DssRecordType;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mil.army.usace.hec.dss.internal.hecdss_h$shared.*;

public final class CatalogReader {
    private static final int MAX_PATHNAME_LENGTH = 1000;

    private CatalogReader() {}

    public static List<DssPathname> read(DssSession session) {
        return readWithTypes(session).stream()
                .map(DssCatalogEntry::pathname)
                .toList();
    }

    public static List<DssCatalogEntry> readWithTypes(DssSession session) {
        return readWithTypes(session, new DssPathname("*", "*", "*", "*", "*", "*"));
    }

    public static List<DssCatalogEntry> readWithTypes(DssSession session, DssPathname filter) {
        Arena arena = session.arena();

        int count = recordCount(session);
        long bufferLength = Math.multiplyExact((long) MAX_PATHNAME_LENGTH, count);

        MemorySegment pathBuffer = arena.allocate(C_CHAR, bufferLength);
        MemorySegment recordTypeCodes = arena.allocate(C_INT, count);
        MemorySegment pathFilter = arena.allocateFrom(filter.toString());

        hecdss_h.hec_dss_catalog(
                session.dssPointer(), pathBuffer, recordTypeCodes,
                pathFilter, count, MAX_PATHNAME_LENGTH
        );

        List<String> paths = parseNullTerminatedStrings(pathBuffer, bufferLength);
        int[] codes = recordTypeCodes.asSlice(0, (long) count * ValueLayout.JAVA_INT.byteSize())
                .toArray(ValueLayout.JAVA_INT);

        List<DssCatalogEntry> entries = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            Optional<DssPathname> parsed = DssPathname.parse(paths.get(i));
            if (parsed.isPresent()) {
                DssRecordType type = RecordTypeReader.fromNativeCode(i < codes.length ? codes[i] : 0);
                entries.add(new DssCatalogEntry(parsed.get(), type));
            }
        }
        return entries;
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

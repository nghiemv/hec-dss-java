package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssPathname;
import mil.army.usace.hec.dss.internal.natives.ForeignLanguage;
import mil.army.usace.hec.dss.internal.natives.MemoryAllocator;
import mil.army.usace.hec.dss.internal.natives.MemoryParser;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Optional;
import java.util.stream.Stream;

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
        Arena memorySession = dssSession.getMemorySession();
        MemoryAllocator memoryAllocator = MemoryAllocator.create(ForeignLanguage.C, memorySession);

        int recordCount = getRecordCount();
        int charBufferLength = MAX_PATHNAME_LENGTH * recordCount;

        MemorySegment pathBuffer = memoryAllocator.allocateChars(charBufferLength);
        MemorySegment recordTypes = memoryAllocator.allocateInts(recordCount);
        MemorySegment pathFilter = memoryAllocator.allocateString(filterPattern.toString());

        hecdss_h.hec_dss_catalog(
                dssSession.getDssStackPointer(),
                pathBuffer,
                recordTypes,
                pathFilter,
                recordCount,
                MAX_PATHNAME_LENGTH
        );

        return MemoryParser.parseStrings(ForeignLanguage.C, pathBuffer).stream()
                .map(DssPathname::parse)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    int getRecordCount() {
        return hecdss_h.hec_dss_record_count(dssSession.getDssStackPointer());
    }
}

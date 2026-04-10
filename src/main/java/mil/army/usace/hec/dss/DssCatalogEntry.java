package mil.army.usace.hec.dss;

/**
 * A catalog entry pairing a pathname with its record type.
 * Returned by {@link HecDss#getCatalog(java.nio.file.Path)}.
 */
public record DssCatalogEntry(DssPathname pathname, DssRecordType recordType) {}

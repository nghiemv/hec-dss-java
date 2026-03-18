package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HecDssCatalogReadTest {

    @Test
    void readAllCatalogEntries() {
        Path dssFile = TestUtil.getResourceFile("examples-all-data-types.dss");
        List<DssCatalogEntry> catalog = HecDss.getCatalog(dssFile);
        assertEquals(208, catalog.size());
        // Every entry should have a pathname and a record type
        for (DssCatalogEntry entry : catalog) {
            assertNotNull(entry.pathname());
            assertNotNull(entry.recordType());
        }
    }
}

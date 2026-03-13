package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HecDssCatalogTest {
    @Test
    void catalogRetrieveAll() throws Exception {
        Path dssFile = TestUtil.getResourceFile("examples-all-data-types.dss");
        List<DssPathname> catalog = HecDss.getCatalog(dssFile);
        assertEquals(208, catalog.size());
    }
}

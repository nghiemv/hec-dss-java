package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HecDssCatalogTest {
    @Test
    void catalogRetrieveAll() throws Exception {
        String dssFilePath = TestUtil.getResourceFile("examples-all-data-types.dss").toString();
        List<DssPathname> catalog = HecDss.getCatalog(dssFilePath);
        assertEquals(208, catalog.size());
    }
}

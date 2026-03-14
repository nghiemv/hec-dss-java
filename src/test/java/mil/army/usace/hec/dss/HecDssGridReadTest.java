package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssGridReadTest {

    @Test
    void readGrid() {
        Path dssFile = TestUtil.getResourceFile("grid-example.dss");
        String pathname = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS/";

        DssGrid grid = HecDss.readGrid(dssFile, pathname);
        assertEquals(21, grid.numberOfCellsX());
        assertEquals(28, grid.numberOfCellsY());
        assertEquals(21 * 28, grid.data().length);
    }
}

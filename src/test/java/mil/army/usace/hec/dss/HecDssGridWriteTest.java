package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssGridWriteTest {

    @Test
    void readGridWriteToNewPathThenRead() {
        Path dssFile = TestUtil.copyResourceToTemp("grid-example.dss");
        String readPath = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS/";
        String writePath = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS-copy/";

        DssGrid original = HecDss.readGrid(dssFile, readPath);
        HecDss.writeGrid(dssFile, writePath, original);

        DssGrid reread = HecDss.readGrid(dssFile, writePath);
        assertEquals(original.width(), reread.width());
        assertEquals(original.height(), reread.height());

        double[][] origValues = original.values();
        double[][] rereadValues = reread.values();
        for (int row = 0; row < original.height(); row++) {
            assertArrayEquals(origValues[row], rereadValues[row], 0.001,
                    "Row %d mismatch".formatted(row));
        }
    }

    @Test
    void writeNewGridThenRead() {
        Path dssFile = TestUtil.createTempFile("grid-write-test.dss");
        String pathname = "/grid/new/gradient/01MAY2024:1400/01MAY2024:1400/new-grad/";

        int width = 50, height = 50;
        double cellSize = 2000.0;
        double[][] values = new double[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[row][col] = col + (50 * row);
            }
        }

        double[] x = new double[width];
        double[] y = new double[height];
        for (int col = 0; col < width; col++) {
            x[col] = (col + 0.5) * cellSize;
        }
        for (int row = 0; row < height; row++) {
            y[row] = (height - 1 - row + 0.5) * cellSize; // north → south
        }

        DssGrid input = DssGrid.of(values, x, y, "MM", Crs.SHG);

        HecDss.writeGrid(dssFile, pathname, input);

        DssGrid output = HecDss.readGrid(dssFile, pathname);
        assertEquals(width, output.width());
        assertEquals(height, output.height());
        for (int row = 0; row < height; row++) {
            assertArrayEquals(values[row], output.values()[row], 0.01,
                    "Row %d mismatch".formatted(row));
        }
    }

    @Test
    void gridOrientationRowZeroIsNorth() {
        int width = 3, height = 4;
        double cellSize = 1000.0;

        // Row 0 (north) = 1.0, row 1 = 2.0, ..., row 3 (south) = 4.0
        double[][] values = new double[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[row][col] = row + 1;
            }
        }

        double[] x = new double[width];
        double[] y = new double[height];
        for (int col = 0; col < width; col++) {
            x[col] = (col + 0.5) * cellSize;
        }
        for (int row = 0; row < height; row++) {
            y[row] = (height - 1 - row + 0.5) * cellSize;
        }

        Path dssFile = TestUtil.createTempFile("grid-orientation-test.dss");
        String pathname = "/grid/TEST/ORIENTATION/01JAN2020:0000/01JAN2020:0000/DEBUG/";

        DssGrid input = DssGrid.of(values, x, y, "MM", Crs.SHG);
        HecDss.writeGrid(dssFile, pathname, input);
        DssGrid output = HecDss.readGrid(dssFile, pathname);

        // Verify row 0 = north (value 1.0), row 3 = south (value 4.0)
        assertEquals(1.0, output.value(0, 0), 0.01, "row 0 (north) should be 1.0");
        assertEquals(4.0, output.value(height - 1, 0), 0.01, "last row (south) should be 4.0");

        // Verify all rows
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                assertEquals(row + 1, output.value(row, col), 0.01,
                        "value(%d,%d) mismatch".formatted(row, col));
            }
        }
    }
}

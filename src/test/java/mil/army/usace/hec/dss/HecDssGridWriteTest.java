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
        assertArrayEquals(original.data(), reread.data(), 0.001);
        assertEquals(original.numberOfCellsX(), reread.numberOfCellsX());
        assertEquals(original.numberOfCellsY(), reread.numberOfCellsY());
    }

    @Test
    void writeNewGridThenRead() {
        Path dssFile = TestUtil.createTempFile("grid-write-test.dss");
        String pathname = "/grid/new/gradient/01MAY2024:1400/01MAY2024:1400/new-grad/";

        int cellsX = 50, cellsY = 50;
        double[] data = new double[cellsX * cellsY];
        for (int i = 0; i < cellsY; i++) {
            for (int j = 0; j < cellsX; j++) {
                data[i * cellsX + j] = j + (50 * i);
            }
        }

        DssGrid input = DssGrid.of(data, cellsX, cellsY, 2000.0, 0.0, 0.0)
                .withSrs("WKT", "");

        HecDss.writeGrid(dssFile, pathname, input);

        DssGrid output = HecDss.readGrid(dssFile, pathname);
        assertEquals(cellsX, output.numberOfCellsX());
        assertEquals(cellsY, output.numberOfCellsY());
        assertArrayEquals(data, output.data(), 0.01);
    }

    @Test
    void debugGridDataOrdering() {
        System.out.println("=== PART 1: Write and read a small grid with known pattern ===");

        int cellsX = 3, cellsY = 4;
        double[] data = new double[cellsX * cellsY];
        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }

        System.out.println("Input data (linear): ");
        for (int i = 0; i < data.length; i++) {
            System.out.printf("  data[%2d] = %.0f%n", i, data[i]);
        }

        System.out.println("\nInput interpreted as row-major (row 0 = first cellsX values):");
        for (int row = 0; row < cellsY; row++) {
            System.out.printf("  row %d: ", row);
            for (int col = 0; col < cellsX; col++) {
                System.out.printf("%5.0f", data[row * cellsX + col]);
            }
            System.out.println();
        }

        Path dssFile = TestUtil.createTempFile("grid-ordering-test.dss");
        String pathname = "/grid/TEST/ORDERING/01JAN2020:0000/01JAN2020:0000/DEBUG/";

        DssGrid input = DssGrid.of(data, cellsX, cellsY, 2000.0, 0.0, 0.0)
                .withSrs("WKT", "");

        HecDss.writeGrid(dssFile, pathname, input);
        DssGrid output = HecDss.readGrid(dssFile, pathname);

        double[] outData = output.data();
        System.out.println("\nOutput data (linear): ");
        for (int i = 0; i < outData.length; i++) {
            System.out.printf("  data[%2d] = %.0f%n", i, outData[i]);
        }

        System.out.println("\nOutput interpreted as row-major (row 0 = first cellsX values):");
        for (int row = 0; row < output.numberOfCellsY(); row++) {
            System.out.printf("  row %d: ", row);
            for (int col = 0; col < output.numberOfCellsX(); col++) {
                System.out.printf("%5.0f", outData[row * output.numberOfCellsX() + col]);
            }
            System.out.println();
        }

        System.out.printf("\nOutput grid dims: cellsX=%d, cellsY=%d%n",
                output.numberOfCellsX(), output.numberOfCellsY());
        System.out.printf("Output lowerLeftCellX=%d, lowerLeftCellY=%d%n",
                output.lowerLeftCellX(), output.lowerLeftCellY());

        boolean identical = true;
        for (int i = 0; i < data.length; i++) {
            if (Math.abs(data[i] - outData[i]) > 0.01) {
                identical = false;
                break;
            }
        }
        System.out.println("\nData round-tripped identically: " + identical);

        // PART 2: Also write a grid with distinct row values to make orientation obvious
        System.out.println("\n=== PART 2: Row-labeled grid (top=1, bottom=4) ===");
        double[] rowData = new double[cellsX * cellsY];
        for (int row = 0; row < cellsY; row++) {
            for (int col = 0; col < cellsX; col++) {
                rowData[row * cellsX + col] = row + 1;
            }
        }

        System.out.println("Input (intended: row0=1.0, row1=2.0, row2=3.0, row3=4.0):");
        for (int row = 0; row < cellsY; row++) {
            System.out.printf("  row %d: ", row);
            for (int col = 0; col < cellsX; col++) {
                System.out.printf("%5.1f", rowData[row * cellsX + col]);
            }
            System.out.println();
        }

        String pathname2 = "/grid/TEST/ORDERING/01JAN2020:0000/01JAN2020:0000/DEBUG-ROWS/";
        DssGrid input2 = DssGrid.of(rowData, cellsX, cellsY, 2000.0, 0.0, 0.0)
                .withSrs("WKT", "");
        HecDss.writeGrid(dssFile, pathname2, input2);
        DssGrid output2 = HecDss.readGrid(dssFile, pathname2);
        double[] outData2 = output2.data();

        System.out.println("Output:");
        for (int row = 0; row < output2.numberOfCellsY(); row++) {
            System.out.printf("  row %d: ", row);
            for (int col = 0; col < output2.numberOfCellsX(); col++) {
                System.out.printf("%5.1f", outData2[row * output2.numberOfCellsX() + col]);
            }
            System.out.println();
        }

        // PART 3: Read the existing grid-example.dss and print first/last values
        System.out.println("\n=== PART 3: Existing grid-example.dss ===");
        Path existingFile = TestUtil.copyResourceToTemp("grid-example.dss");
        String existingPath = "/grid/EAU GALLA RIVER/SNOW MELT/02FEB2020:0600/03FEB2020:0600/SHG-SNODAS/";
        DssGrid existing = HecDss.readGrid(existingFile, existingPath);
        double[] existingData = existing.data();

        System.out.printf("Grid dims: cellsX=%d, cellsY=%d, total=%d%n",
                existing.numberOfCellsX(), existing.numberOfCellsY(), existingData.length);
        System.out.printf("lowerLeftCellX=%d, lowerLeftCellY=%d%n",
                existing.lowerLeftCellX(), existing.lowerLeftCellY());
        System.out.printf("cellSize=%.1f, xOrigin=%.1f, yOrigin=%.1f%n",
                existing.cellSize(), existing.xCoordOfGridCellZero(), existing.yCoordOfGridCellZero());
        System.out.printf("gridType=%s, dataType=%s%n", existing.gridType(), existing.dataType());
        System.out.printf("min=%.4f, max=%.4f, mean=%.4f%n",
                existing.minDataValue(), existing.maxDataValue(), existing.meanDataValue());

        System.out.println("\nFirst row (row 0, indices 0..cellsX-1):");
        for (int col = 0; col < existing.numberOfCellsX(); col++) {
            System.out.printf("  [0][%d] = %.4f%n", col, existingData[col]);
        }

        System.out.println("Last row (row " + (existing.numberOfCellsY() - 1) + "):");
        int lastRowStart = (existing.numberOfCellsY() - 1) * existing.numberOfCellsX();
        for (int col = 0; col < existing.numberOfCellsX(); col++) {
            System.out.printf("  [%d][%d] = %.4f%n", existing.numberOfCellsY() - 1, col,
                    existingData[lastRowStart + col]);
        }

        System.out.println("\nAll rows (first column value only):");
        for (int row = 0; row < existing.numberOfCellsY(); row++) {
            double firstVal = existingData[row * existing.numberOfCellsX()];
            double lastVal = existingData[row * existing.numberOfCellsX() + existing.numberOfCellsX() - 1];
            System.out.printf("  row %2d: first=%.4f, last=%.4f%n", row, firstVal, lastVal);
        }
    }
}

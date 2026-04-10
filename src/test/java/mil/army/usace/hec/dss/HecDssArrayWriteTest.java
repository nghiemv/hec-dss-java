package mil.army.usace.hec.dss;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HecDssArrayWriteTest {

    @Test
    void writeAndReadArray() {
        Path dssFile = TestUtil.createTempFile("array-test.dss");
        String pathname = "/test/array/data////";

        HecDss.writeArray(dssFile, pathname, new double[]{1.0, 3.0, 5.0, 7.0});

        double[] output = HecDss.readArray(dssFile, pathname);
        assertArrayEquals(new double[]{1.0, 3.0, 5.0, 7.0}, output, 0.01);
    }

    @Test
    void writeModifyWriteReadArray() {
        Path dssFile = TestUtil.createTempFile("array-modify-test.dss");
        String path1 = "/TEST/LOCATION/DATA///ARRAY-ORIG/";
        String path2 = "/TEST/LOCATION/DATA///ARRAY-MODIFIED/";

        HecDss.writeArray(dssFile, path1, new double[]{10.0, 20.0, 30.0});

        double[] read = HecDss.readArray(dssFile, path1);

        for (int i = 0; i < read.length; i++) {
            read[i] *= 2;
        }
        HecDss.writeArray(dssFile, path2, read);

        double[] reread = HecDss.readArray(dssFile, path2);
        assertArrayEquals(new double[]{20.0, 40.0, 60.0}, reread, 0.01);
    }
}

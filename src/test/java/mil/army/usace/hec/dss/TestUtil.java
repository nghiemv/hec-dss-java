package mil.army.usace.hec.dss;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class TestUtil {
    private static final Logger logger = Logger.getLogger(TestUtil.class.getName());

    public static Path getResourceFile(String pathFromResource) {
        if (!pathFromResource.startsWith("/")) {
            pathFromResource = "/" + pathFromResource;
        }

            URL url = TestUtil.class.getResource(pathFromResource);
            if (url == null) return null;


        Path rval = null;
        try {
            rval = Paths.get(url.toURI());
        }
        catch (Exception e) {
            return null;
        }

        return rval;

    }

    public static String createTempFile(String fileName) {
        try {
            int index = fileName.lastIndexOf(".");
            boolean hasExtension = index > 0;
            String prefix = hasExtension ? fileName.substring(0, index) : fileName;
            String suffix = hasExtension ? fileName.substring(index) : "";
            return Files.createTempFile(prefix, suffix).toAbsolutePath().toString();
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }
}
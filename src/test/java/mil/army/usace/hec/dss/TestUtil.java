package mil.army.usace.hec.dss;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TestUtil {

    public static Path getResourceFile(String pathFromResource) {
        if (!pathFromResource.startsWith("/")) {
            pathFromResource = "/" + pathFromResource;
        }
        URL url = TestUtil.class.getResource(pathFromResource);
        if (url == null) {
            throw new AssertionError("Test resource not found: " + pathFromResource);
        }
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError("Invalid resource URI: " + pathFromResource, e);
        }
    }

    public static Path createTempFile(String fileName) {
        int index = fileName.lastIndexOf(".");
        boolean hasExtension = index > 0;
        String prefix = hasExtension ? fileName.substring(0, index) : fileName;
        String suffix = hasExtension ? fileName.substring(index) : "";
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temp file: " + fileName, e);
        }
    }

    public static Path copyResourceToTemp(String resourceName) {
        Path source = getResourceFile(resourceName);
        try {
            Path temp = Files.createTempFile(resourceName.replace(".dss", ""), ".dss");
            Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot copy resource to temp: " + resourceName, e);
        }
    }
}

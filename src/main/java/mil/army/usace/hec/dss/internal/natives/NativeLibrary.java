package mil.army.usace.hec.dss.internal.natives;

import org.scijava.nativelib.NativeLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum NativeLibrary {
    HEC_DSS("hecdss");

    private static final Logger logger = Logger.getLogger(NativeLibrary.class.getName());
    private final String libraryName;

    NativeLibrary(String libraryName) {
        this.libraryName = libraryName;
    }

    public void initialize() {
        try {
            NativeLoader.loadLibrary(this.libraryName);
        } catch (Exception exception) {
            String errorMessage = String.format("Failed to load native library: %s", this.libraryName);
            throw new RuntimeException(errorMessage, exception);
        }
    }
}

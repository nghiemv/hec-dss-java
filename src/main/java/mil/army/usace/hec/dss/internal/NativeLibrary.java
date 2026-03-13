package mil.army.usace.hec.dss.internal;

import org.scijava.nativelib.NativeLoader;

enum NativeLibrary {
    HEC_DSS("hecdss");

    private final String libraryName;

    NativeLibrary(String libraryName) {
        this.libraryName = libraryName;
    }

    void initialize() {
        try {
            NativeLoader.loadLibrary(this.libraryName);
        } catch (Exception exception) {
            String errorMessage = String.format("Failed to load native library: %s", this.libraryName);
            throw new RuntimeException(errorMessage, exception);
        }
    }
}

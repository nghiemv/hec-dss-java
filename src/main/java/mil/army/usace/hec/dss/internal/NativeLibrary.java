package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import org.scijava.nativelib.NativeLoader;

final class NativeLibrary {
    private static final String LIBRARY_NAME = "hecdss";
    private static volatile boolean loaded;

    private NativeLibrary() {}

    static void load() throws DssException {
        if (loaded) return;
        try {
            NativeLoader.loadLibrary(LIBRARY_NAME);
            loaded = true;
        } catch (Exception e) {
            throw new DssException(
                    "Cannot load native library '%s'".formatted(LIBRARY_NAME), e);
        }
    }
}

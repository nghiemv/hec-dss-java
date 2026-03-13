package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.DssException;
import org.scijava.nativelib.NativeLoader;

final class NativeLibrary {
    private static volatile boolean loaded;

    private NativeLibrary() {}

    static void load() throws DssException {
        if (loaded) return;
        try {
            NativeLoader.loadLibrary("hecdss");
            loaded = true;
        } catch (Exception e) {
            throw new DssException("Failed to load native library: hecdss", e);
        }
    }
}

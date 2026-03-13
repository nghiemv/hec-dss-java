package mil.army.usace.hec.dss.internal.natives;

import java.lang.foreign.ValueLayout;

public enum ForeignLanguage {
    C;

    /* Language's Memory ValueLayout */
    public ValueLayout getCharLayout() {
        return switch (this) {
            case C -> ValueLayout.JAVA_BYTE;
        };
    }

    public ValueLayout getIntLayout() {
        return switch (this) {
            case C -> ValueLayout.JAVA_INT;
        };
    }

    public ValueLayout getDoubleLayout() {
        return switch (this) {
            case C -> ValueLayout.JAVA_DOUBLE;
        };
    }

    public ValueLayout getFloatLayout() {
        return switch (this) {
            case C -> ValueLayout.JAVA_FLOAT;
        };
    }
}

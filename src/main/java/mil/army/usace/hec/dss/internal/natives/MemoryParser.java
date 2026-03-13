package mil.army.usace.hec.dss.internal.natives;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public interface MemoryParser {
    static int parseInt(MemorySegment memorySegment) {
        return memorySegment.get(ValueLayout.JAVA_INT, 0);
    }

    static int[] parseInts(MemorySegment memorySegment) {
        return memorySegment.toArray(ValueLayout.JAVA_INT);
    }

    static String parseString(MemorySegment memorySegment) {
        return memorySegment.getString(0);
    }

    static List<String> parseStrings(ForeignLanguage foreignLanguage, MemorySegment memorySegment) {
        String bufferContent = StandardCharsets.ISO_8859_1.decode(memorySegment.asByteBuffer()).toString();
        return Arrays.stream(bufferContent.split(getStringTerminatorChar(foreignLanguage)))
                .filter(x -> !x.isBlank())
                .toList();
    }

    private static String getStringTerminatorChar(ForeignLanguage foreignLanguage) {
        return switch (foreignLanguage) {
            case C -> "\0";
        };
    }

    static double[] parseDoubles(MemorySegment memorySegment) {
        return memorySegment.toArray(ValueLayout.JAVA_DOUBLE);
    }

    static float parseFloat(MemorySegment memorySegment) {
        return memorySegment.get(ValueLayout.JAVA_FLOAT, 0);
    }

    static float[] parseFloats(MemorySegment memorySegment) {
        return memorySegment.toArray(ValueLayout.JAVA_FLOAT);
    }
}

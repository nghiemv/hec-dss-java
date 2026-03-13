package mil.army.usace.hec.dss.internal.natives;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface MemoryAllocator {
    MemorySegment allocateChars(int characterCount);
    MemorySegment allocateInts(int integerCount);
    MemorySegment allocateDoubles(int doubleCount);
    MemorySegment allocateString(String stringToAllocate);
    MemorySegment allocatePointer();

    static MemoryAllocator create(ForeignLanguage foreignLanguage, Arena memorySession) {
        return new MemoryAllocatorImpl(foreignLanguage, memorySession);
    }
}

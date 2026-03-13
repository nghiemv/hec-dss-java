package mil.army.usace.hec.dss.internal.natives;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

final class MemoryAllocatorImpl implements MemoryAllocator {
    private final ForeignLanguage foreignLanguage;
    private final Arena memorySession;

    public MemoryAllocatorImpl(ForeignLanguage foreignLanguage, Arena memorySession) {
        this.foreignLanguage = foreignLanguage;
        this.memorySession = memorySession;
    }

    @Override
    public MemorySegment allocateChars(int characterCount) {
        return memorySession.allocate(foreignLanguage.getCharLayout(), characterCount);
    }

    @Override
    public MemorySegment allocateInts(int integerCount) {
        return memorySession.allocate(foreignLanguage.getIntLayout(), integerCount);
    }

    @Override
    public MemorySegment allocateDoubles(int doubleCount) {
        return memorySession.allocate(foreignLanguage.getDoubleLayout(), doubleCount);
    }

    @Override
    public MemorySegment allocateString(String stringToAllocate) {
        return memorySession.allocateFrom(stringToAllocate);
    }

    @Override
    public MemorySegment allocatePointer() {
        return memorySession.allocate(ValueLayout.ADDRESS);
    }
}

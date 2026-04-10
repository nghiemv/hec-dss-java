package mil.army.usace.hec.dss;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.stream.Stream;

class DssPathnameTest {

    private static Stream<Arguments> providePatternMatchCases() {
        DssPathname path1 = new DssPathname("A", "B", "C", "D", "E", "F");
        DssPathname path2 = new DssPathname("X", "B", "C", "D", "E", "F");

        DssPathname exactPattern = new DssPathname("A", "B", "C", "D", "E", "F");
        DssPathname wildcardA = new DssPathname("*", "B", "C", "D", "E", "F");
        DssPathname wildcardF = new DssPathname("A", "B", "C", "D", "E", "*");
        DssPathname wildcardAF = new DssPathname("*", "B", "C", "D", "E", "*");
        DssPathname nonMatchingPattern = new DssPathname("Z", "B", "C", "D", "E", "F");

        return Stream.of(
                Arguments.of(path1, exactPattern, true),
                Arguments.of(path1, wildcardA, true),
                Arguments.of(path1, wildcardF, true),
                Arguments.of(path1, wildcardAF, true),
                Arguments.of(path1, nonMatchingPattern, false),
                Arguments.of(path2, exactPattern, false),
                Arguments.of(path2, wildcardA, true),
                Arguments.of(path2, nonMatchingPattern, false)
        );
    }

    @Test
    void constructorShouldCreateValidPathname() {
        DssPathname path = new DssPathname("A", "B", "C", "D", "E", "F");
        assertEquals("/A/B/C/D/E/F/", path.toString());
        assertEquals("A", path.aPart());
        assertEquals("B", path.bPart());
        assertEquals("C", path.cPart());
        assertEquals("D", path.dPart());
        assertEquals("E", path.ePart());
        assertEquals("F", path.fPart());
    }

    @Test
    void constructorShouldRejectNullParts() {
        assertThrows(IllegalArgumentException.class, () ->
                new DssPathname("A", null, "C", "D", "E", "F"));
    }

    @Test
    void constructorShouldRejectInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                new DssPathname("A", "B/", "C", "D", "E", "F"));
        assertThrows(IllegalArgumentException.class, () ->
                new DssPathname("A", "B", "C", "D", "E", "F\n"));
    }

    @Test
    void parseShouldHandleValidPathname() {
        Optional<DssPathname> result = DssPathname.parse("/A/B/C/D/E/F/");
        assertTrue(result.isPresent());
        assertEquals("A", result.get().aPart());
        assertEquals("B", result.get().bPart());
        assertEquals("C", result.get().cPart());
        assertEquals("D", result.get().dPart());
        assertEquals("E", result.get().ePart());
        assertEquals("F", result.get().fPart());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "A/B/C/D/E/F/",
            "/A/B/C/D/E/F",
            "/A/B/C/D/E/",
            "/A/B/C/D/E/F/G/"
    })
    void parseShouldReturnEmptyForInvalidPathname(String input) {
        Optional<DssPathname> result = DssPathname.parse(input);
        assertFalse(result.isPresent());
    }

    @Test
    void isValidShouldCheckPathnames() {
        assertTrue(DssPathname.isValid("/A/B/C/D/E/F/"));
        assertFalse(DssPathname.isValid("/A/B/C/D/E/"));
        assertFalse(DssPathname.isValid(null));
    }

    @Test
    void withShouldReturnNewPathnameWithChangedPart() {
        DssPathname original = new DssPathname("A", "B", "C", "D", "E", "F");
        DssPathname modified = original.with(DssPathname.Part.C, "NewC");

        assertEquals("A", modified.aPart());
        assertEquals("B", modified.bPart());
        assertEquals("NewC", modified.cPart());
        assertEquals("D", modified.dPart());
        assertEquals("E", modified.ePart());
        assertEquals("F", modified.fPart());

        assertNotSame(original, modified);
    }

    @Test
    void partShouldReturnRequestedPart() {
        DssPathname path = new DssPathname("A", "B", "C", "D", "E", "F");
        assertEquals("A", path.part(DssPathname.Part.A));
        assertEquals("B", path.part(DssPathname.Part.B));
        assertEquals("C", path.part(DssPathname.Part.C));
        assertEquals("D", path.part(DssPathname.Part.D));
        assertEquals("E", path.part(DssPathname.Part.E));
        assertEquals("F", path.part(DssPathname.Part.F));
    }

    @Test
    void isPatternShouldDetectWildcards() {
        assertFalse(new DssPathname("A", "B", "C", "D", "E", "F").isPattern());
        assertTrue(new DssPathname("*", "B", "C", "D", "E", "F").isPattern());
        assertTrue(new DssPathname("A", "B", "C", "D", "E", "*").isPattern());
    }

    @ParameterizedTest
    @MethodSource("providePatternMatchCases")
    void matchesShouldCheckPatternCorrectly(
            DssPathname path, DssPathname pattern, boolean expected) {
        assertEquals(expected, path.matches(pattern));
    }

    @Test
    void toPatternShouldCreateWildcardPattern() {
        DssPathname path = new DssPathname("A", "B", "C", "D", "E", "F");

        DssPathname pattern1 = path.toPattern(DssPathname.Part.A);
        assertEquals("*", pattern1.aPart());
        assertEquals("B", pattern1.bPart());

        DssPathname pattern2 = path.toPattern(DssPathname.Part.A, DssPathname.Part.F);
        assertEquals("*", pattern2.aPart());
        assertEquals("B", pattern2.bPart());
        assertEquals("C", pattern2.cPart());
        assertEquals("D", pattern2.dPart());
        assertEquals("E", pattern2.ePart());
        assertEquals("*", pattern2.fPart());

        assertTrue(pattern1.isPattern());
        assertTrue(pattern2.isPattern());
    }

    @Test
    void toStringShouldFormatPathnameCorrectly() {
        DssPathname path = new DssPathname("A", "B", "C", "D", "E", "F");
        assertEquals("/A/B/C/D/E/F/", path.toString());

        DssPathname pattern = path.toPattern(DssPathname.Part.C, DssPathname.Part.E);
        assertEquals("/A/B/*/D/*/F/", pattern.toString());
    }
}
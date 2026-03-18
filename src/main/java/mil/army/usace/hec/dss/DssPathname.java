package mil.army.usace.hec.dss;

import java.util.Optional;

/**
 * Represents a DSS pathname with parts A through F.
 * All instances are guaranteed to be valid.
 *
 * <p><b>Case sensitivity:</b> DSS pathnames are case-insensitive but case-preserving.
 * Parts are stored in their original case, but {@link #equals}, {@link #hashCode},
 * and {@link #matches} all compare case-insensitively — matching the native DSS
 * behavior (zhash uppercases before hashing, zpathnameCompare uses toupper).
 */
public record DssPathname(String aPart, String bPart, String cPart, String dPart, String ePart, String fPart) {
    private static final String WILDCARD = "*";

    /**
     * Validates all parts during construction.
     *
     * @throws IllegalArgumentException if any part is invalid
     */
    public DssPathname {
        validatePart(aPart);
        validatePart(bPart);
        validatePart(cPart);
        validatePart(dPart);
        validatePart(ePart);
        validatePart(fPart);
    }

    private static void validatePart(String part) {
        if (part == null) {
            throw new IllegalArgumentException("Path parts cannot be null");
        }
        for (int i = 0; i < part.length(); i++) {
            char c = part.charAt(i);
            if (c == '/' || Character.isISOControl(c)) {
                throw new IllegalArgumentException(
                        "Path part contains invalid characters: " + part);
            }
        }
    }

    /**
     * Attempts to parse a string into a DssPathname.
     *
     * @param pathname The DSS pathname string
     * @return An Optional containing the parsed DssPathname, or empty if invalid
     */
    public static Optional<DssPathname> parse(String pathname) {
        try {
            if (pathname == null || !pathname.startsWith("/") || !pathname.endsWith("/")) {
                return Optional.empty();
            }

            String[] parts = pathname.substring(1, pathname.length() - 1).split("/", -1);

            if (parts.length != 6) {
                return Optional.empty();
            }

            return Optional.of(new DssPathname(
                    parts[0].trim(), parts[1].trim(), parts[2].trim(),
                    parts[3].trim(), parts[4].trim(), parts[5].trim()
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a string represents a valid DSS pathname.
     */
    public static boolean isValid(String pathname) {
        return parse(pathname).isPresent();
    }

    private static boolean matchesPart(String value, String pattern) {
        return WILDCARD.equals(pattern) || pattern.equalsIgnoreCase(value);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof DssPathname p
                && aPart.equalsIgnoreCase(p.aPart)
                && bPart.equalsIgnoreCase(p.bPart)
                && cPart.equalsIgnoreCase(p.cPart)
                && dPart.equalsIgnoreCase(p.dPart)
                && ePart.equalsIgnoreCase(p.ePart)
                && fPart.equalsIgnoreCase(p.fPart));
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = 31 * h + aPart.toUpperCase().hashCode();
        h = 31 * h + bPart.toUpperCase().hashCode();
        h = 31 * h + cPart.toUpperCase().hashCode();
        h = 31 * h + dPart.toUpperCase().hashCode();
        h = 31 * h + ePart.toUpperCase().hashCode();
        h = 31 * h + fPart.toUpperCase().hashCode();
        return h;
    }

    /**
     * Creates a new DssPathname with the specified part changed.
     */
    public DssPathname with(Part part, String value) {
        return switch (part) {
            case A -> new DssPathname(value, bPart, cPart, dPart, ePart, fPart);
            case B -> new DssPathname(aPart, value, cPart, dPart, ePart, fPart);
            case C -> new DssPathname(aPart, bPart, value, dPart, ePart, fPart);
            case D -> new DssPathname(aPart, bPart, cPart, value, ePart, fPart);
            case E -> new DssPathname(aPart, bPart, cPart, dPart, value, fPart);
            case F -> new DssPathname(aPart, bPart, cPart, dPart, ePart, value);
        };
    }

    /**
     * Gets the specified part of the pathname.
     */
    public String getPart(Part part) {
        return switch (part) {
            case A -> aPart;
            case B -> bPart;
            case C -> cPart;
            case D -> dPart;
            case E -> ePart;
            case F -> fPart;
        };
    }

    /**
     * Returns the full DSS pathname string.
     */
    @Override
    public String toString() {
        return "/" + String.join("/", aPart, bPart, cPart, dPart, ePart, fPart) + "/";
    }

    /**
     * Checks if this pathname matches a pattern.
     * A pattern is a pathname where "*" in any part matches any value.
     *
     * @param pattern The pattern to match against
     * @return true if this pathname matches the pattern
     */
    public boolean matches(DssPathname pattern) {
        if (pattern == null) return false;

        return matchesPart(aPart, pattern.aPart) &&
                matchesPart(bPart, pattern.bPart) &&
                matchesPart(cPart, pattern.cPart) &&
                matchesPart(dPart, pattern.dPart) &&
                matchesPart(ePart, pattern.ePart) &&
                matchesPart(fPart, pattern.fPart);
    }

    /**
     * Creates a wildcard pattern from this pathname by replacing
     * the specified parts with "*".
     *
     * @param parts The parts to convert to wildcards
     * @return A new pathname with wildcards in the specified positions
     */
    public DssPathname toPattern(Part... parts) {
        String a = aPart;
        String b = bPart;
        String c = cPart;
        String d = dPart;
        String e = ePart;
        String f = fPart;

        for (Part part : parts) {
            switch (part) {
                case A -> a = WILDCARD;
                case B -> b = WILDCARD;
                case C -> c = WILDCARD;
                case D -> d = WILDCARD;
                case E -> e = WILDCARD;
                case F -> f = WILDCARD;
            }
        }

        return new DssPathname(a, b, c, d, e, f);
    }

    /**
     * Checks if this pathname is a pattern (contains any wildcard parts).
     *
     * @return true if this pathname contains any "*" wildcards
     */
    public boolean isPattern() {
        return WILDCARD.equals(aPart) || WILDCARD.equals(bPart) || WILDCARD.equals(cPart)
                || WILDCARD.equals(dPart) || WILDCARD.equals(ePart) || WILDCARD.equals(fPart);
    }

    /**
     * Enumeration of the path parts.
     */
    public enum Part {A, B, C, D, E, F}
}
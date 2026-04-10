package mil.army.usace.hec.dss.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Centralizes normalization of strings written to the DSS native library.
 *
 * <p>Only normalizes values where DSS validates against a known set (intervals).
 * Units, parameters, and other free-form strings are passed through as-is
 * because DSS does not validate them and normalization could lose information
 * (e.g. "pH" → "PH").
 */
public final class NativeStrings {
    private NativeStrings() {}

    private static final Map<String, String> INTERVAL_CANONICAL = new HashMap<>();
    static {
        String[] intervals = {
            "1Second", "2Second", "3Second", "4Second", "5Second", "6Second",
            "10Second", "15Second", "20Second", "30Second",
            "1Minute", "2Minute", "3Minute", "4Minute", "5Minute", "6Minute",
            "10Minute", "12Minute", "15Minute", "20Minute", "30Minute",
            "1Hour", "2Hour", "3Hour", "4Hour", "6Hour", "8Hour", "12Hour",
            "1Day", "1Week", "Tri-Month", "Semi-Month", "1Month", "1Year",
            "IR-Day", "IR-Month", "IR-Year", "IR-Decade", "IR-Century"
        };
        for (String s : intervals) {
            INTERVAL_CANONICAL.put(s.toUpperCase(Locale.ROOT), s);
        }
    }

    /**
     * Normalizes an E-part interval string to the canonical mixed-case spelling
     * from {@code standardIntervals.h}. Returns the input unchanged if not a
     * recognized interval.
     */
    public static String normalizeInterval(String ePart) {
        if (ePart == null) return "";
        String canonical = INTERVAL_CANONICAL.get(ePart.toUpperCase(Locale.ROOT));
        return canonical != null ? canonical : ePart;
    }
}

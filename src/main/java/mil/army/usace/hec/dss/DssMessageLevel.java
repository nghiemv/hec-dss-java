package mil.army.usace.hec.dss;

/**
 * How much diagnostic output the native DSS library writes to its message
 * destination (stdout by default; see {@link HecDss#setLogFile}).
 *
 * <p>The level is global to the process, not per file or per call. It defaults
 * to {@link #GENERAL}, which prints a header for every {@code zopen} and a
 * nineteen-line statistics block for every {@code zclose} — several hundred
 * lines per test suite. Applications that surface failures through
 * {@link DssException} rather than by scraping stdout will normally want
 * {@link #CRITICAL}.
 *
 * <p>The constants mirror the {@code MESS_LEVEL_*} scale in heclib's
 * {@code zsetMessageLevel.c}. Each is higher-is-noisier and includes everything
 * below it.
 */
public enum DssMessageLevel {

    /**
     * No messages at all, including errors. heclib itself calls this "highly
     * discouraged", and suppression is not guaranteed — some paths write
     * regardless. Prefer {@link #CRITICAL}.
     */
    NONE,

    /** Critical (error) messages only. */
    CRITICAL,

    /** Terse: {@code zopen}, {@code zclose}, and critical errors. */
    TERSE,

    /** General log messages. The native library's default. */
    GENERAL,

    /** Diagnostic messages aimed at users, such as echoed input parameters. */
    USER_DIAGNOSTIC,

    /** Internal debug output, level 1. Not intended for end users. */
    INTERNAL_DIAGNOSTIC_1,

    /** Internal debug output, level 2 (full debug). */
    INTERNAL_DIAGNOSTIC_2
}

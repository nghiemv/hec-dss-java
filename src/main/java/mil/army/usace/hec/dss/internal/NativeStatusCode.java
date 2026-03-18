package mil.army.usace.hec.dss.internal;

/**
 * Maps native DSS status codes to human-readable messages.
 *
 * @see <a href="https://github.com/HydrologicEngineeringCenter/hec-dss/blob/master/heclib/heclib_c/src/headers/zerrorCodes.h">
 *      zerrorCodes.h</a>
 */
public final class NativeStatusCode {
    private NativeStatusCode() {}

    private static final String[] MESSAGES = {
        /* 0  */ "OK",
        /* 1  */ "Invalid file version",
        /* 2  */ "Incompatible version",
        /* 3  */ "Incompatible version (DSS6 — convert to DSS7)",
        /* 4  */ "Invalid file name",
        /* 5  */ "No exclusive access",
        /* 6  */ "Unable to access file",
        /* 7  */ "Unable to write file",
        /* 8  */ "Unable to create file",
        /* 9  */ "No write permission",
        /* 10 */ "No permission",
        /* 11 */ "Write on read-only file",
        /* 12 */ "Invalid DSS file",
        /* 13 */ "Invalid address",
        /* 14 */ "Invalid number to read",
        /* 15 */ "Invalid number to write",
        /* 16 */ "Write error",
        /* 17 */ "Read error",
        /* 18 */ "Read beyond end of file",
        /* 19 */ "Invalid file header",
        /* 20 */ "Truncated file",
        /* 21 */ "Invalid header parameter",
        /* 22 */ "Damaged file",
        /* 23 */ "File is closed",
        /* 24 */ "Empty file",
        /* 25 */ "Internal table corrupt",
        /* 26 */ "Key corrupt",
        /* 27 */ "Key value error",
        /* 28 */ "Key location error",
        /* 29 */ "Cannot lock file",
        /* 30 */ "Cannot lock for exclusive access",
        /* 31 */ "Cannot lock for multi-user access",
        /* 32 */ "Cannot squeeze",
        /* 33 */ "Invalid bin status",
        /* 34 */ "Cannot allocate memory",
        /* 35 */ "Invalid parameter",
        /* 36 */ "Invalid number",
        /* 37 */ "Incompatible call",
        /* 38 */ "Non-empty file",
        /* 39 */ "Bin size conflict",
        /* 40 */ "Different record type",
        /* 41 */ "Wrong record type",
        /* 42 */ "Cannot undelete with reclaim",
        /* 43 */ "Array space exhausted",
        /* 44 */ "Both note kinds used",
        /* 45 */ "No data given",
        /* 46 */ "No data read",
        /* 47 */ "No time window",
        /* 48 */ "Invalid date/time",
        /* 49 */ "Invalid interval",
        /* 50 */ "Times not ascending",
        /* 51 */ "Different profile number",
        /* 52 */ "Record does not exist",
        /* 53 */ "Record already exists",
        /* 54 */ "Invalid pathname",
        /* 55 */ "Invalid record header",
        /* 56 */ "Array too small",
        /* 57 */ "Not opened",
        /* 58 */ "File does not exist",
        /* 59 */ "File exists",
        /* 60 */ "Null filename",
        /* 61 */ "Null pathname",
        /* 62 */ "Null argument",
        /* 63 */ "Null array",
        /* 64 */ "Vertical datum error",
        /* 65 */ "Invalid F-part tags",
        /* 66 */ "Undefined error",
    };

    public static String describe(int code) {
        if (code >= 0 && code < MESSAGES.length) {
            return MESSAGES[code];
        }
        return "Unknown error (code " + code + ")";
    }
}

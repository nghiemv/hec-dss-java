package mil.army.usace.hec.dss.internal.util;

public class PrimitiveArrayUtil {
    private PrimitiveArrayUtil() {
        // Utility Class
    }

    /* Trim Arrays */
    public static int[] trimArray(int[] array, int targetCount) {
        int[] trimmedArray = new int[targetCount];
        System.arraycopy(array, 0, trimmedArray, 0, targetCount);
        return trimmedArray;
    }

    public static double[] trimArray(double[] array, int targetCount) {
        double[] trimmedArray = new double[targetCount];
        System.arraycopy(array, 0, trimmedArray, 0, targetCount);
        return trimmedArray;
    }
}

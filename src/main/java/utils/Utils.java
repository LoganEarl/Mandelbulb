package utils;

public class Utils {
    private static final int ACOS_LOOKUP_TABLE_RES = 1024 * 1024 * 32;
    private static double[] acosLookupTable = null;

    static {
        long start = System.currentTimeMillis();
        acosLookupTable = new double[ACOS_LOOKUP_TABLE_RES];
        for (int i = 0; i < ACOS_LOOKUP_TABLE_RES; i++) {
            double xInput = i / (double) ACOS_LOOKUP_TABLE_RES * 2 - 1;
            acosLookupTable[i] = Math.acos(xInput);
        }
        System.out.printf("Finished creating lookup table in %dms\n", System.currentTimeMillis() - start);
    }

    public static float constrain(float amt, float low, float high) {
        return amt < low ? low : (Math.min(amt, high));
    }

    public static int constrainInt(int amt, int low, int high) {
        return amt < low ? low : (Math.min(amt, high));
    }


//    public static Vector3 dynamicInterpolate(float scalar, Vector3 keyFrame0, Vector3 keyFrame1, Vector3... keyFrameN) {
//        //We need to use cosine interpolation if there are less than 4 keyframes.
//
//        //If >=
//
//    }

    public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
    }

    public static float sqrt(float n) {
        return (float) Math.sqrt(n);
    }

    public static float sq(float n) {
        return n * n;
    }

    public static float cubicInterpolate(float start, float end, float preStart, float postEnd, float scalar) {
        float squaredScalar = scalar * scalar;
        float a0 = postEnd - end - preStart + start;
        float a1 = preStart - start - a0;
        float a2 = end - preStart;

        return a0 * scalar * squaredScalar +
                a1 * squaredScalar +
                a2 * scalar +
                start;
    }

    public static float cosineInterpolate(float start, float end, float scalar) {
        float angle = scalar * (float) Math.PI;
        float newScalar = (1f - (float) Math.cos(angle)) * 0.5f;
        return linearInterpolate(start, end, newScalar);
    }

    public static float linearInterpolate(float start, float end, float scalar) {
        return (end - start) * scalar * start;
    }

    public static float sigmoid(float in) {
        return 1 / (1 + pow(2.71828f, -1f * in));
    }

    public static float pow(float n, float e) {
        return (float) Math.pow((double) n, (double) e);
    }

    public static double fastAcos(double x) {
        x = Math.min(Math.max(x, -1), 1);

        int lookupIndex = (int) ((x + 1) / 2 * ACOS_LOOKUP_TABLE_RES);
        if (lookupIndex == ACOS_LOOKUP_TABLE_RES) {
            lookupIndex = ACOS_LOOKUP_TABLE_RES - 1;
        }
        return acosLookupTable[lookupIndex];
    }
}

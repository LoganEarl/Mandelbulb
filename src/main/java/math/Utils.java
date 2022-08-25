package math;

import java.awt.*;

public class Utils {
    public static float constrain(float amt, float low, float high) {
        return amt < low ? low : (Math.min(amt, high));
    }

    public static int constrainInt(int amt, int low, int high) {
        return amt < low ? low : (Math.min(amt, high));
    }

    public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
    }

    public static float sqrt(float n) {
        return (float)Math.sqrt(n);
    }

    public static float sq(float n) {
        return n * n;
    }

    public static Color interpolate(Color startColor, Color endColor, float scalar) {
        float[] startComponents = startColor.getColorComponents(new float[3]);
        float[] endComponents = endColor.getColorComponents(new float[3]);

        float redDiff = endComponents[0] - startComponents[0];
        float greenDiff = endComponents[1] - startComponents[1];
        float blueDiff = endComponents[2] - startComponents[2];

        return new Color(
                startComponents[0] + (redDiff * scalar),
                startComponents[1] + (greenDiff * scalar),
                startComponents[2] + (blueDiff * scalar)
        );
    }

    public static float sigmoid(float in) {
        return 1 / (1 + pow(2.71828f, -1f * in));
    }

    public static float pow(float n, float e) {
        return (float)Math.pow((double)n, (double)e);
    }
}

package utils;

import static java.lang.Math.abs;

public class ColorUtils {
    public static final int WHITE = color(255, 255, 255, 255);
    public static final int GRAY = color(128, 128, 128, 255);
    public static final int BLACK = color(0, 0, 0, 255);

    public static final int RED = color(255, 0, 0, 255);
    public static final int GREEN = color(0, 255, 0, 255);
    public static final int BLUE = color(0, 0, 255, 255);

    public static int red(int rgbColor) {
        return (rgbColor >> 16) & 0xff;
    }

    public static int green(int rgbColor) {
        return (rgbColor >> 8) & 0xff;
    }

    public static int blue(int rgbColor) {
        return rgbColor & 0xff;
    }

    public static float redF(int rgbColor) {
        return red(rgbColor)/255f;
    }

    public static float greenF(int rgbColor) {
        return green(rgbColor)/255f;
    }

    public static float blueF(int rgbColor) {
        return blue(rgbColor)/255f;
    }


    public static int colorF(float r, float g, float b, float a) {
        return color((int) (r * 255f), (int) (g * 255f), (int) (b * 255f), (int) (a * 255f));
    }

    //Takes a number from 0 to 1 and maps it to an rgb value
    public static int colorFromScalar(float s) {
        float red = (float)(0.5f + (Math.sin(Math.PI * 2 * (s - 9/12f)))/2f);
        float green = (float)(0.5f + (Math.sin(Math.PI * 2 * (s - 1/12f)))/2f);
        float blue = (float)(0.5f + (Math.sin(Math.PI * 2 * (s - 5/12f)))/2f);
        return colorF(red, green, blue, 1);
    }

    public static int color(int r, int g, int b, int a) {
        r = Utils.constrainInt(r, 0, 255);
        g = Utils.constrainInt(g, 0, 255);
        b = Utils.constrainInt(b, 0, 255);
        a = Utils.constrainInt(a, 0, 255);

        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF));
    }

    public static int interpolate(int startColor, int endColor, float scalar) {

        int redDiff = abs(red(startColor) - red(endColor));
        int greenDiff = abs(green(startColor) - green(endColor));
        int blueDiff = abs(blue(startColor) - blue(endColor));

        return color(
                red(startColor) + (int) (redDiff * scalar),
                green(startColor) + (int) (greenDiff * scalar),
                blue(startColor) + (int) (blueDiff * scalar),
                255
        );
    }
}

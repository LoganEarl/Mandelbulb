package shape;

import lombok.Builder;
import lombok.NoArgsConstructor;
import utils.Utils;
import utils.Vector3;
import renderer.Drawable;

import javax.vecmath.Vector3d;
import java.awt.*;

import static utils.ColorUtils.interpolate;
import static processing.core.PApplet.*;

@NoArgsConstructor
public class Bulb implements Drawable {
    private static final float DISTANCE = 1.5f;


    private Vector3 position;
    private float reflectivity;
    private int steps;
    private float period;
    private float granularity;
    private int startColor;
    private int endColor;
    private int glowColor;
    private float glowIntensity;

    @Builder
    public Bulb(Vector3 position, float reflectivity, int steps, float period, float granularity, int startColor, int endColor, int glowColor, float glowIntensity) {
        this.position = position;
        this.reflectivity = reflectivity;
        this.steps = steps;
        this.period = period;
        this.granularity = granularity;
        this.startColor = startColor;
        this.endColor = endColor;
        this.glowColor = glowColor;
        this.glowIntensity = glowIntensity;
    }

    public float distanceToSurface(int timeIndex, Vector3 point) {
        Vector3 p = new Vector3(point).subV(position);

        float[] rdr = iterate(timeIndex, p);
        return 0.5f * log(rdr[0]) * rdr[0] / rdr[1];
    }

    //Iterates the given point for the given time index. If it converges, it is part of the fractal. It is outside if not.
    private float[] iterate(int timeIndex, Vector3 point) {
        double power = 5.0f * (sin(timeIndex * period) + Math.PI/16) + 7.0;

        Vector3d z = new Vector3d(point);
        double dr = 1.0;
        double r = 0.0;
        int i;
        for (i = 0; i < steps; i++) {
            r = z.length();
            if (r == 0) r = granularity;
            if (r > DISTANCE) break;

            // convert to polar coordinates
            double theta = Utils.fastAcos(z.z / r);
            double phi = Math.atan2(z.y, z.x);
            dr = Math.pow(r, power - 1.0f) * power * dr + 1.0f;

            // scale and rotate the point
            double zr = Math.pow(r, power);
            theta = theta * power;
            phi = phi * power;

            // convert back to cartesian coordinates
            z.set(Math.sin(theta) * Math.cos(phi), Math.sin(phi) * Math.sin(theta), Math.cos(theta));
            z.scale(zr);
            z.add(new Vector3d(point));
        }
        return new float[]{(float)r, (float)dr, i};
    }

    @Override
    public Vector3 getNormalAtSurface(int timeIndex, Vector3 position, Vector3 origin) {
        Vector3 normal = new Vector3();

        normal.set(position.x + granularity, position.y, position.z);
        float xPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x - granularity, position.y, position.z);
        float xMinus = distanceToSurface(timeIndex, normal);

        normal.set(position.x, position.y + granularity, position.z);
        float yPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x, position.y - granularity, position.z);
        float yMinus = distanceToSurface(timeIndex, normal);

        normal.set(position.x, position.y, position.z + granularity);
        float zPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x, position.y, position.z - granularity);
        float zMinus = distanceToSurface(timeIndex, normal);

        return normal.setV(xPlus - xMinus, yPlus - yMinus, zPlus - zMinus)
                        .normalizeV();
    }


    public Vector3 getPosition() {
        return position;
    }

    public int getColor(int timeIndex, Vector3 position) {
        if(startColor == endColor) {
            return startColor;
        }
        float[] rdr = iterate(timeIndex, position);

//        Some magic numbers that look good
        float scale = constrain(rdr[2] * 2 / steps, 0, 1);

        return interpolate(startColor, endColor, scale);
    }

    public float getReflectivity() {
        return reflectivity;
    }

    @Override
    public float getBaseGlowIntensity() {
        return glowIntensity;
    }

    @Override
    public int getGlowColor() {
        return glowColor;
    }

    @Override
    public DrawableType getDrawableType() {
        return DrawableType.BULB;
    }


}

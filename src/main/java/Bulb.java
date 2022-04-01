import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static processing.core.PApplet.*;

public class Bulb implements Drawable{
    private final Vector3f position;
    private final float reflectivity;
    private final int steps;
    private static final float DISTANCE = 1.5f;
    private final float period;
    private final float granularity;
    private final Color startColor;
    private final Color endColor;

    public Bulb(Vector3f position, float reflectivity, int steps, float period, float granularity, Color startColor, Color endColor) {
        this.position = position;
        this.reflectivity = reflectivity;
        this.steps = steps;
        this.period = period;
        this.granularity = granularity;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public float distanceToSurface(int timeIndex, Vector3f point){
        float[] rdr = iterate(timeIndex, point);
        return 0.5f* log(rdr[0])*rdr[0]/rdr[1];
    }

    //Iterates the given point for the given time index. If it converges, it is part of the fractal. It is outside if not.
    private float[] iterate(int timeIndex, Vector3f point){
        float power = 3.0f+4.0f*(sin(timeIndex*period))+1.0f;
        Vector3f z = new Vector3f(point);

        float dr = 1.0f;
        float r = 0.0f;
        int i;
        for (i = 0; i < steps ; i++) {
            r = z.length();
            if (r>DISTANCE) break;

            // convert to polar coordinates
            float theta = acos(z.z/r);
            float phi = atan2(z.y,z.x);
            dr =  pow( r, power-1.0f)*power*dr + 1.0f;

            // scale and rotate the point
            float zr = pow( r,power);
            theta = theta*power;
            phi = phi*power;

            // convert back to cartesian coordinates
            z.set(sin(theta)* cos(phi), sin(phi)* sin(theta), cos(theta));
            z.scale(zr);
            z.add(point);
        }
        return new float[]{r, dr, (float)i};
    }

    @Override
    public Vector3f getNormalAtSurface(int timeIndex, Vector3f position) {
        Vector3f normal = new Vector3f();

        normal.set(position.x + granularity,position.y, position.z);
        float xPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x - granularity,position.y, position.z);
        float xMinus = distanceToSurface(timeIndex, normal);

        normal.set(position.x,position.y + granularity, position.z);
        float yPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x,position.y - granularity, position.z);
        float yMinus = distanceToSurface(timeIndex, normal);

        normal.set(position.x,position.y, position.z + granularity);
        float zPlus = distanceToSurface(timeIndex, normal);
        normal.set(position.x,position.y, position.z - granularity);
        float zMinus = distanceToSurface(timeIndex, normal);

        normal.set(xPlus - xMinus, yPlus - yMinus,zPlus - zMinus);
        normal.normalize();

        return normal;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Color getColor(int timeIndex, Vector3f position) {
        float[] rdr = iterate(timeIndex, position);

        if(rdr[1] > 10000000000f) rdr[1] = 10000000000f;
        float scale = log(rdr[1]) * (float)Math.PI;

        //Some magic numbers that look good
        scale = constrain((scale-5)/15, 0, 1);

//        float[] baseColor = new float[3];
//        baseColor[0] = constrain(cos(scale),0,1);
//        baseColor[1] = constrain(sin(scale),0,1);
//        baseColor[2] = constrain(cos(scale + (float)Math.PI),0,1);
//        return new Color(baseColor[0],baseColor[1],baseColor[2]);

        float[] startComponents = startColor.getColorComponents(new float[3]);
        float[] endComponents = endColor.getColorComponents(new float[3]);

        float redDiff = endComponents[0] - startComponents[0];
        float greenDiff = endComponents[1] - startComponents[1];
        float blueDiff = endComponents[2] - startComponents[2];

        return new Color(
                startComponents[0] + (redDiff * scale),
                startComponents[1] + (greenDiff * scale),
                startComponents[2] + (blueDiff * scale)
        );
    }

    public float getReflectivity() {
        return reflectivity;
    }
}

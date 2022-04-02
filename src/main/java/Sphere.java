import javax.vecmath.Vector3f;
import java.awt.*;

public class Sphere implements Drawable{
    private final Vector3f position;
    private final Color color;
    private final float radius;
    private final float reflectivity;
    private final float glowIntensity;

    public Sphere(Vector3f position, Color color, float radius, float reflectivity, float glowIntensity) {
        this.position = position;
        this.color = color;
        this.radius = radius;
        this.reflectivity = reflectivity;
        this.glowIntensity = glowIntensity;
    }

    private float distanceToCenter(Vector3f point){
        Vector3f vec = new Vector3f(position);
        vec.sub(point);
        return vec.length();
    }

    public float distanceToSurface(int timeIndex, Vector3f point){
        return distanceToCenter(point) - radius;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Color getColor(int timeIndex, Vector3f position) {
        return color;
    }

    @Override
    public Vector3f getNormalAtSurface(int timeIndex, Vector3f position) {
        Vector3f normal = new Vector3f(position);
        normal.sub(this.position);
        normal.normalize();
        return normal;
    }

    public float getRadius() {
        return radius;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    @Override
    public float getBaseGlowIntensity() {
        return glowIntensity;
    }

    @Override
    public Color getGlowColor() {
        return color;
    }
}

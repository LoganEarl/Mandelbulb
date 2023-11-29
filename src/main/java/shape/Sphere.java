package shape;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.Vector3;
import renderer.Drawable;

import javax.vecmath.Vector3f;
import java.awt.*;

@Data
@NoArgsConstructor
public class Sphere implements Drawable {
    private Vector3 position;
    private int color;
    private float radius;
    private float reflectivity;
    private float glowIntensity;

    public Sphere(Vector3 position, int color, float radius, float reflectivity, float glowIntensity) {
        this.position = position;
        this.color = color;
        this.radius = radius;
        this.reflectivity = reflectivity;
        this.glowIntensity = glowIntensity;
    }

    private float distanceToCenter(Vector3f point) {
        Vector3f vec = new Vector3f(position);
        vec.sub(point);
        return vec.length();
    }

    public float distanceToSurface(int timeIndex, Vector3 point) {
        return Math.abs(radius - distanceToCenter(point));
    }

    public Vector3 getPosition() {
        return position;
    }

    public int getColor(int timeIndex, Vector3 position) {
        return color;
    }

    @Override
    public Vector3 getNormalAtSurface(int timeIndex, Vector3 position, Vector3 origin) {
        Vector3 normal = new Vector3(position)
                .subV(this.position)
                .normalizeV();

        if (distanceToCenter(origin) < radius) {
            normal.negate();
        }

        return normal;
    }

    @Override
    @JsonIgnore
    public float getBaseGlowIntensity() {
        return glowIntensity;
    }

    @Override
    @JsonIgnore
    public int getGlowColor() {
        return color;
    }

    @Override
    public DrawableType getDrawableType() {
        return DrawableType.SPHERE;
    }
}

package shape;

import renderer.Drawable;

import javax.vecmath.Vector3f;
import java.awt.*;

public class RepeatingShape implements Drawable {
    private final Drawable toRepeat;
    private final float repeatDistance;

    public RepeatingShape(float repeatDistance, Drawable toRepeat) {
        this.toRepeat = toRepeat;
        this.repeatDistance = repeatDistance;
    }

    private static float modByDomain(float in, float domain) {
        float out = (in + domain/2) % domain;
        if(out < 0) out += domain;
        out -= domain/2;
        return out;
    }

    private Vector3f repeatPoint(Vector3f point){
        return new Vector3f(
                modByDomain(point.x, repeatDistance),
                modByDomain(point.y, repeatDistance),
                modByDomain(point.z, repeatDistance)
        );
    }

    @Override
    public float distanceToSurface(int timeIndex, Vector3f point) {
        return toRepeat.distanceToSurface(timeIndex, repeatPoint(point));
    }

    @Override
    public Vector3f getPosition() {
        return repeatPoint(toRepeat.getPosition());
    }

    @Override
    public Vector3f getNormalAtSurface(int timeIndex, Vector3f position, Vector3f origin) {
        return toRepeat.getNormalAtSurface(timeIndex, repeatPoint(position), repeatPoint(origin));
    }

    @Override
    public Color getColor(int timeIndex, Vector3f position) {
        return toRepeat.getColor(timeIndex, repeatPoint(position));
    }

    @Override
    public float getReflectivity() {
        return toRepeat.getReflectivity();
    }

    @Override
    public float getBaseGlowIntensity() {
        return toRepeat.getBaseGlowIntensity();
    }

    @Override
    public Color getGlowColor() {
        return toRepeat.getGlowColor();
    }
}

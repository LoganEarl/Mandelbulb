package shape;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.Vector3;
import renderer.Drawable;

import java.awt.*;

@Data
@NoArgsConstructor
public class RepeatingShape implements Drawable {
    private Drawable toRepeat;
    private float repeatDistance;

    public RepeatingShape(float repeatDistance, Drawable toRepeat) {
        this.toRepeat = toRepeat;
        this.repeatDistance = repeatDistance;
    }

    private static float modByDomain(float in, float domain) {
        float out = (in + domain / 2) % domain;
        while (out < 0) out += domain;
        out -= domain / 2;
        return out;
    }

    private Vector3 repeatPoint(Vector3 point) {
        return new Vector3(
                modByDomain(point.x, repeatDistance),
                modByDomain(point.y, repeatDistance),
                modByDomain(point.z, repeatDistance)
        );
    }

    @Override
    public float distanceToSurface(int timeIndex, Vector3 point) {
        return toRepeat.distanceToSurface(timeIndex, repeatPoint(point));
    }

    @Override
    @JsonIgnore
    public Vector3 getPosition() {
        return repeatPoint(toRepeat.getPosition());
    }

    @Override
    @JsonIgnore
    public Vector3 getNormalAtSurface(int timeIndex, Vector3 position, Vector3 origin) {
        return toRepeat.getNormalAtSurface(timeIndex, repeatPoint(position), repeatPoint(origin));
    }

    @Override
    @JsonIgnore
    public int getColor(int timeIndex, Vector3 position) {
        return toRepeat.getColor(timeIndex, repeatPoint(position));
    }

    @Override
    @JsonIgnore
    public float getReflectivity() {
        return toRepeat.getReflectivity();
    }

    @Override
    @JsonIgnore
    public float getBaseGlowIntensity() {
        return toRepeat.getBaseGlowIntensity();
    }

    @Override
    @JsonIgnore
    public int getGlowColor() {
        return toRepeat.getGlowColor();
    }

    @Override
    public DrawableType getDrawableType() {
        return DrawableType.REPEATING_SHAPE;
    }
}

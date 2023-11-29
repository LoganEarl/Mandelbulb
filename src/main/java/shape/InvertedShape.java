package shape;

import lombok.Data;
import lombok.NoArgsConstructor;
import renderer.Drawable;
import utils.Vector3;

import java.awt.*;

@Data
@NoArgsConstructor
public class InvertedShape implements Drawable {
    private Drawable toInvert;

    public InvertedShape(Drawable toInvert) {
        this.toInvert = toInvert;
    }

    @Override
    public float distanceToSurface(int timeIndex, Vector3 point) {
        //TODO fix this, needs some fancy shmancy work with spheres and projections
        return toInvert.distanceToSurface(timeIndex, point) * -1;
    }

    @Override
    public Vector3 getPosition() {
        return toInvert.getPosition();
    }

    @Override
    public Vector3 getNormalAtSurface(int timeIndex, Vector3 position, Vector3 origin) {
        return toInvert.getNormalAtSurface(timeIndex, position, origin).negateV();
    }

    @Override
    public int getColor(int timeIndex, Vector3 position) {
        return toInvert.getColor(timeIndex, position);
    }

    @Override
    public float getReflectivity() {
        return toInvert.getReflectivity();
    }

    @Override
    public float getBaseGlowIntensity() {
        return toInvert.getBaseGlowIntensity();
    }

    @Override
    public int getGlowColor() {
        return toInvert.getGlowColor();
    }

    @Override
    public DrawableType getDrawableType() {
        return DrawableType.INVERTED_SHAPE;
    }
}

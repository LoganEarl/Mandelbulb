import javax.vecmath.Vector3f;
import java.awt.*;

public interface Drawable {
    float distanceToSurface(int timeIndex, Vector3f point);
    Vector3f getPosition();
    Vector3f getNormalAtSurface(int timeIndex, Vector3f position);
    Color getColor(int timeIndex, Vector3f position);
    float getReflectivity();
}

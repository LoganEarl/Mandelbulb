package renderer;

import javax.vecmath.Vector3f;
import java.awt.*;

public interface RayEngine {
    Color calculateRay(Vector3f direction, Vector3f position, int timeIndex);
}

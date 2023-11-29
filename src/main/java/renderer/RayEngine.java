package renderer;

import utils.Vector3;
import settings.RayEngineSettings;

import java.awt.*;

public interface RayEngine {
    int calculateRay(Vector3 direction, Vector3 position, int timeIndex);
    RayEngineSettings getSettings();
}

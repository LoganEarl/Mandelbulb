package renderer;

import lombok.Getter;
import lombok.experimental.Delegate;
import utils.Vector3;
import settings.CameraSettings;

@Getter
public abstract class Camera {
    @Delegate
    private CameraSettings settings;

    public Camera(CameraSettings settings) {
        this.settings = settings;
    }
    abstract void getPixelPosition(int x, int y, Vector3 modify);
    abstract void getPixelDirection(int x, int y, Vector3 modify);
}

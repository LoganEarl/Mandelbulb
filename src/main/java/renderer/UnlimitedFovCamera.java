package renderer;

import utils.Vector3;
import settings.CameraSettings;

public class UnlimitedFovCamera extends Camera {
    private final float aspectRatio;

    public UnlimitedFovCamera(CameraSettings cameraSettings) {
        super(cameraSettings);
        aspectRatio = getScreenWidth() / (float) getScreenHeight();
    }

    public void getPixelPosition(int x, int y, Vector3 modify) {
        modify.set(this.getPosition());
    }

    public void getPixelDirection(int x, int y, Vector3 modify) {
        modify.set(this.getDirection());

        float widthOffsetAngle = x / (float) getScreenWidth();
        float fov = 3f * (float) Math.PI / 8f;
        widthOffsetAngle = (widthOffsetAngle * fov - fov / 2f) * aspectRatio;

        float heightOffsetAngle = y / (float) getScreenHeight();
        heightOffsetAngle = heightOffsetAngle * fov - fov / 2f;

        modify.rotate(getUpVector(), widthOffsetAngle)
                .rotate(getSideVector(), heightOffsetAngle);
    }
}

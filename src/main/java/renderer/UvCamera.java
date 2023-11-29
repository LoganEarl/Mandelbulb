package renderer;

import utils.Vector3;
import settings.CameraSettings;

public class UvCamera extends Camera {
    private final float pixelXIncrement;
    private final float pixelYIncrement;

    public UvCamera(CameraSettings cameraSettings) {
        super(cameraSettings);

        float fovRadians = getFovRadians();
        float screenHeight = getScreenHeight();
        float screenWidth = getScreenWidth();

        if(fovRadians >= Math.PI || fovRadians <= 0)
            throw new IllegalArgumentException("Fov is too high for this camera implementation");

        pixelXIncrement = (float) Math.tan(fovRadians / 2) / (screenWidth / 2f);

        double aspectRatio = screenWidth / (double) screenHeight;
        double effectiveVerticalFov = fovRadians / aspectRatio;
        pixelYIncrement = (float) Math.tan(effectiveVerticalFov / 2) / (screenHeight / 2f);
    }

    @Override
    void getPixelPosition(int x, int y, Vector3 modify) {
        modify.set(getPosition());
    }

    @Override
    void getPixelDirection(int x, int y, Vector3 modify) {
        int xOffset = x - getScreenWidth() / 2;
        int yOffset = y - getScreenHeight() / 2;

        Vector3 yComponent = this.getUpVector().clone()
                .normalizeV()
                .scaleV(yOffset * pixelYIncrement)
                .negateV();

        modify.setV(this.getSideVector())
                .normalizeV()
                .scaleV(xOffset * pixelXIncrement)
                .addV(getDirection())
                .addV(yComponent)
                .normalizeV();
    }
}

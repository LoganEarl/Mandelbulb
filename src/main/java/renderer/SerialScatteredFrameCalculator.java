package renderer;

import lombok.Builder;

import javax.vecmath.Vector3f;

public class SerialScatteredFrameCalculator extends FrameCalculator {
    private static final int SCANNING_INDEX_CAP = 10;
    private final RayEngine rayEngine;

    private int scanningIndexX = 0;
    private int scanningIndexY = 0;
    private boolean shouldRestartCurrentFrame = false;

    @Builder
    public SerialScatteredFrameCalculator(int width, int height, Camera camera, int[] pixelBuffer, FrameCompleteCallback callback, RayEngine rayEngine) {
        super(width, height, camera, pixelBuffer, callback);
        this.rayEngine = rayEngine;
    }

    @Override
    public void update(int timeIndex) {
        Vector3f position = new Vector3f();
        Vector3f direction = new Vector3f();

        for (int y = scanningIndexY; y < getHeight(); y += SCANNING_INDEX_CAP) {
            for (int x = scanningIndexX; x < getWidth(); x += SCANNING_INDEX_CAP) {
                if (shouldRestartCurrentFrame) break;
                getCamera().getPixelPosition(x, y, position);
                getCamera().getPixelDirection(x, y, direction);

                this.writePixel(x, y, rayEngine.calculateRay(direction, position, timeIndex).getRGB());
            }
            if (shouldRestartCurrentFrame) {
                shouldRestartCurrentFrame = false;
                break;
            }
        }

        scanningIndexX++;
        if (scanningIndexX >= SCANNING_INDEX_CAP) {
            scanningIndexX = scanningIndexX % SCANNING_INDEX_CAP;
            scanningIndexY++;
        }

        //If we rendered all of our scan lines save the frame and advance to the next one
        if (scanningIndexY > SCANNING_INDEX_CAP) {
            scanningIndexY = scanningIndexY % SCANNING_INDEX_CAP;
            super.markFrameComplete();
        }
    }

    public void triggerFrameRestart(){
        this.shouldRestartCurrentFrame = true;
    }
}

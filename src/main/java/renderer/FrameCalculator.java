package renderer;

import lombok.Getter;
import lombok.Setter;
import utils.TimingData;

//Holds a reference to a buffer of pixels. Can draw to it in a variety of ways and patterns
public abstract class FrameCalculator {
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final Camera camera;
    private final int[] pixelBuffer;
    private final FrameCompleteCallback callback;
    @Getter
    @Setter
    private boolean frameLogEnabled = false;

    private final TimingData frameTiming = new TimingData();
    private final TimingData scanPassTiming = new TimingData();

    public FrameCalculator(int width, int height, Camera camera, int[] pixelBuffer, FrameCompleteCallback callback) {
        this.width = width;
        this.height = height;
        this.camera = camera;
        this.pixelBuffer = pixelBuffer;
        this.callback = callback;
    }

    protected void writePixel(int x, int y, int color) {
        frameTiming.markTimingStarted();
        scanPassTiming.markTimingStarted();

        pixelBuffer[x + y * width] = color;
    }

    protected void markScanPassComplete() {
        scanPassTiming.markTimingComplete();
    }

    protected void markFrameComplete() {
        frameTiming.markTimingComplete();

        this.callback.onFrameComplete(pixelBuffer);
    }

    public void printScanPassRenderStats(int currentProgress, int targetProgress) {
        scanPassTiming.printTimingStats(currentProgress, targetProgress);
    }

    public void printFrameRenderStats(int currentProgress, int targetProgress) {
        frameTiming.printTimingStats(currentProgress, targetProgress);
    }
    public abstract void triggerFrameRestart();

    public abstract void update(int timeIndex);

    public interface FrameCompleteCallback {
        void onFrameComplete(int[] pixels);
    }
}

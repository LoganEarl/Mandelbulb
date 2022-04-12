package renderer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

//Holds a reference to a buffer of pixels. Can draw to it in a variety of ways and patterns
@RequiredArgsConstructor
public abstract class FrameCalculator {
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final Camera camera;
    private final int[] pixelBuffer;
    private final FrameCompleteCallback callback;

    private final long[] lastFrameTimings = new long[10];
    private int currentFrameTimingIndex = -1;
    private long lastFrameFirstWriteTime = 0;
    private long firstFrameWriteTime = 0;

    protected void writePixel(int x, int y, int color){
        long now = System.currentTimeMillis();
        if(firstFrameWriteTime == 0) firstFrameWriteTime = now;
        if(lastFrameFirstWriteTime == 0) lastFrameFirstWriteTime = now;

        pixelBuffer[x + y * width] = color;
    }

    protected void markFrameComplete(){
        long timeToCompleteFrame = System.currentTimeMillis() - lastFrameFirstWriteTime;
        lastFrameFirstWriteTime = 0;
        if(currentFrameTimingIndex == -1){
            Arrays.fill(lastFrameTimings, timeToCompleteFrame);
            currentFrameTimingIndex = 1;
        } else {
            lastFrameTimings[currentFrameTimingIndex++] = timeToCompleteFrame;
            currentFrameTimingIndex = currentFrameTimingIndex % lastFrameTimings.length;
        }

        this.callback.onFrameComplete(pixelBuffer);
    }

    public void printRenderStats(int currentTimeIndex, int targetTimeIndex) {
        long now = System.currentTimeMillis();

        long totalMilliseconds = now - firstFrameWriteTime;
        long averageMSPerFrame = Arrays.stream(lastFrameTimings).sum() / lastFrameTimings.length;

        int secondsToComplete = (int) ((targetTimeIndex - currentTimeIndex) * averageMSPerFrame / 1000);

        int completionHours, completionMinutes, completionSeconds;
        completionHours = secondsToComplete / 60 / 60;
        completionMinutes = secondsToComplete / 60 - completionHours * 60;
        completionSeconds = secondsToComplete - completionMinutes * 60 - completionHours * 60 * 60;

        int totalSeconds = (int) (totalMilliseconds / 1000);
        int elapsedHours, elapsedMinutes, elapsedSeconds;
        elapsedHours = totalSeconds / 60 / 60;
        elapsedMinutes = totalSeconds / 60 - elapsedHours * 60;
        elapsedSeconds = totalSeconds - elapsedMinutes * 60 - elapsedHours * 60 * 60;

        System.out.printf("Rendered Frame Number: %d of %d\n" +
                        "Percent Complete: %.2f percent\n" +
                        "Time To Completion: %d hours, %d minutes and %d seconds\n" +
                        "Total Elapsed Time: %d hours, %d minutes and %d seconds\n" +
                        "Last Frame Time: %d seconds\n\n",
                currentTimeIndex + 1, targetTimeIndex,
                (currentTimeIndex + 1) / (float) targetTimeIndex * 100,
                completionHours, completionMinutes, completionSeconds,
                elapsedHours, elapsedMinutes, elapsedSeconds,
                lastFrameTimings[currentFrameTimingIndex] / 1000);
    }

    public abstract void update(int timeIndex);

    public interface FrameCompleteCallback {
        void onFrameComplete(int[] pixels);
    }
}

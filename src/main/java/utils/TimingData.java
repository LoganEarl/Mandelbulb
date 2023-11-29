package utils;

import lombok.Data;

import java.util.Arrays;

@Data
public class TimingData {
    private final long[] lastTimings = new long[10];
    private int currentTimingIndex = -1;
    private long firstWriteTime = 0;
    private long currentFrameFirstWriteTime = 0;

    public void markTimingStarted() {
        long now = System.currentTimeMillis();
        if(firstWriteTime == 0) firstWriteTime = now;
        if(currentFrameFirstWriteTime == 0) currentFrameFirstWriteTime = now;
    }

    public void markTimingComplete() {
        long timeToComplete = System.currentTimeMillis() - currentFrameFirstWriteTime;
        currentFrameFirstWriteTime = 0;
        if(currentTimingIndex == -1) {
            Arrays.fill(lastTimings, timeToComplete);
            currentTimingIndex = 1;
        } else {
            lastTimings[currentTimingIndex++] = timeToComplete;
            currentTimingIndex = currentTimingIndex % lastTimings.length;
        }
    }

    public void printTimingStats(int currentScanPass, int targetScanPasses) {
        long now = System.currentTimeMillis();
        long totalMilliseconds = now - firstWriteTime;
        long averageMsPerScanPass = Arrays.stream(lastTimings).sum() / lastTimings.length;

        int secondsToComplete = (int) ((targetScanPasses - currentScanPass) * averageMsPerScanPass / 1000);

        int completionHours, completionMinutes, completionSeconds;
        completionHours = secondsToComplete / 60 / 60;
        completionMinutes = secondsToComplete / 60 - completionHours * 60;
        completionSeconds = secondsToComplete - completionMinutes * 60 - completionHours * 60 * 60;

        int totalSeconds = (int) (totalMilliseconds / 1000);
        int elapsedHours, elapsedMinutes, elapsedSeconds;
        elapsedHours = totalSeconds / 60 / 60;
        elapsedMinutes = totalSeconds / 60 - elapsedHours * 60;
        elapsedSeconds = totalSeconds - elapsedMinutes * 60 - elapsedHours * 60 * 60;

        System.out.printf("Completion: (%d/%d) %.2f%% Remaining Time: %dh %dm %ds of %dh %dm %ds Frame Timing: %.2f sec/frame\r",
                currentScanPass + 1, targetScanPasses,
                (currentScanPass + 1) / (float) targetScanPasses * 100,
                completionHours, completionMinutes, completionSeconds,
                elapsedHours + completionHours, elapsedMinutes + completionMinutes, elapsedSeconds + completionSeconds,
                lastTimings[currentScanPass] / 1000f);
    }
}
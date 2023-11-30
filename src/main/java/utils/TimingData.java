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
        if (firstWriteTime == 0) firstWriteTime = now;
        if (currentFrameFirstWriteTime == 0) currentFrameFirstWriteTime = now;
    }

    public void markTimingComplete() {
        long timeToComplete = System.currentTimeMillis() - currentFrameFirstWriteTime;
        currentFrameFirstWriteTime = 0;
        if (currentTimingIndex == -1) {
            Arrays.fill(lastTimings, timeToComplete);
            currentTimingIndex = 1;
        } else {
            lastTimings[currentTimingIndex] = timeToComplete;
            currentTimingIndex = (currentTimingIndex+1) % lastTimings.length;
        }
    }

    public void printTimingStats(int currentScanPass, int targetScanPasses) {
        long averageMsPerScanPass = Arrays.stream(lastTimings).sum() / lastTimings.length;

        int secondsToComplete = (int) ((targetScanPasses - currentScanPass) * averageMsPerScanPass / 1000);
        int totalSeconds = (int) (targetScanPasses * averageMsPerScanPass / 1000);

        int remainingHours, remainingMinutes, remainingSeconds;
        remainingHours = secondsToComplete / 60 / 60;
        remainingMinutes = secondsToComplete / 60 - remainingHours * 60;
        remainingSeconds = secondsToComplete - remainingMinutes * 60 - remainingHours * 60 * 60;

        int totalHours, totalMinutes;
        totalHours = totalSeconds / 60 / 60;
        totalMinutes = totalSeconds / 60 - totalHours * 60;
        totalSeconds = totalSeconds - totalMinutes * 60 - totalHours * 60 * 60;

        int previousTimingIndex = currentTimingIndex - 1;
        while (previousTimingIndex < 0) previousTimingIndex += lastTimings.length;

        System.out.printf("Completion: (%d/%d) %.2f%% Remaining Time: %dh %dm %ds of %dh %dm %ds Frame Timing: %.2f sec/frame (%.2f avg)\r",
                currentScanPass + 1, targetScanPasses,
                (currentScanPass + 1) / (float) targetScanPasses * 100,
                remainingHours, remainingMinutes, remainingSeconds,
                totalHours, totalMinutes, totalSeconds,
                lastTimings[previousTimingIndex] / 1000f, averageMsPerScanPass / 1000f);
    }
}
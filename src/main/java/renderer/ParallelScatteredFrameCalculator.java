package renderer;

import lombok.Builder;
import utils.ColorUtils;
import utils.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ParallelScatteredFrameCalculator extends FrameCalculator {
    private static final int SCANNING_INDEX_CAP = 10;
    private final RayEngine rayEngine;
    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 2)
    );
    private int scanningIndexX = 0;
    private int scanningIndexY = 0;
    private boolean shouldRestartCurrentFrame = false;

    @Builder
    public ParallelScatteredFrameCalculator(int width, int height, Camera camera, int[] pixelBuffer, FrameCompleteCallback callback, RayEngine rayEngine) {
        super(width, height, camera, pixelBuffer, callback);
        this.rayEngine = rayEngine;
    }

    @Override
    public void update(int timeIndex) {
        List<Runnable> drawTasks = new ArrayList<>();
        for (int y = scanningIndexY; y < getHeight(); y += SCANNING_INDEX_CAP) {
            try {
                final int threadY = y;
                drawTasks.add(() -> {
                    Vector3 position = new Vector3();
                    Vector3 direction = new Vector3();

                    for (int x = scanningIndexX; x < getWidth(); x += SCANNING_INDEX_CAP) {
                        if (shouldRestartCurrentFrame) break;
                        getCamera().getPixelPosition(x, threadY, position);
                        getCamera().getPixelDirection(x, threadY, direction);

                        this.writePixel(x, threadY, rayEngine.calculateRay(direction, position, timeIndex));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collection<Future<?>> tasks = new ArrayList<>();
        drawTasks.forEach(drawRunnable -> tasks.add(threadPoolExecutor.submit(drawRunnable)));
        tasks.forEach(task -> {
            try {
                task.get();
            } catch (Exception ignored) {
            }
        });

        if (shouldRestartCurrentFrame) {
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    this.writePixel(x, y, ColorUtils.WHITE);
                }
            }
        }

        if (super.isFrameLogEnabled()) {
            markScanPassComplete();
            printScanPassRenderStats(scanningIndexY * SCANNING_INDEX_CAP + scanningIndexX, SCANNING_INDEX_CAP * SCANNING_INDEX_CAP);
        }

        scanningIndexX++;
        if (scanningIndexX >= SCANNING_INDEX_CAP) {
            scanningIndexX = scanningIndexX % SCANNING_INDEX_CAP;
            scanningIndexY++;
        }

        //If we rendered all of our scan lines save the frame and advance to the next one
        if (scanningIndexY >= SCANNING_INDEX_CAP || shouldRestartCurrentFrame) {
            scanningIndexY = scanningIndexY % SCANNING_INDEX_CAP;
            if (!shouldRestartCurrentFrame) super.markFrameComplete();
            scanningIndexX = 0;
            scanningIndexY = 0;
            shouldRestartCurrentFrame = false;
        }
    }

    public void triggerFrameRestart() {
        this.shouldRestartCurrentFrame = true;
    }
}

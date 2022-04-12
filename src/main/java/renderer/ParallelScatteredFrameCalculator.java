package renderer;

import lombok.Builder;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ParallelScatteredFrameCalculator extends FrameCalculator {
    private static final int SCANNING_INDEX_CAP = 10;
    private final RayEngine rayEngine;

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
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(4);

        List<Runnable> drawTasks = new ArrayList<>();
        for (int y = scanningIndexY; y < getWidth(); y += SCANNING_INDEX_CAP) {
            try {
                final int threadY = y;
                drawTasks.add(() -> {
                    Vector3f position = new Vector3f();
                    Vector3f direction = new Vector3f();

                    for (int x = scanningIndexX; x < getWidth(); x += SCANNING_INDEX_CAP) {
                        if (shouldRestartCurrentFrame) break;
                        getCamera().getPixelPosition(x, threadY, position);
                        getCamera().getPixelDirection(x, threadY, direction);

                        this.writePixel(x, threadY, rayEngine.calculateRay(direction, position, 50).getRGB());
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

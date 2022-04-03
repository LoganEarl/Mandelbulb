import processing.core.PApplet;
import processing.core.PImage;
import processing.event.KeyEvent;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@SuppressWarnings("FieldMayBeFinal")
public class Renderer extends PApplet {
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    private static final float glowHalfDistance = 0.1f;

    //Render Objects
    private List<Drawable> objectsInScene = new ArrayList<>();
    private List<Sphere> lightSources = new ArrayList<>();
    private Camera camera;
    private float rotationPeriod;
    private float cameraDistance = 5f;

    private final Random random = new Random(System.currentTimeMillis());

    //Rendering takes time. This distributes our pixel rendering across the whole image to be more user-friendly
    private int scanningIndexX = 0;
    private int scanningIndexY = 0;
    private static final int SCANNING_INDEX_CAP = 10;
    private long frameRenderStartTime = System.currentTimeMillis(); //needs to be member variable as we take multiple draw steps per frame
    private long totalRenderStartTime = System.currentTimeMillis();

    //Rendering stuff
    private final int lightBounces = 7;
    private final List<Long> frameTimings = new LinkedList<>();
    private int timeIndex = 0;
    private boolean shouldRestartCurrentFrame = true;
    private int imageIndex = 0;
    private static final int targetFrames = 900;
    private List<PImage> frameCaps = new LinkedList<>();
    private MovieConverter movieConverter = new MovieConverter();
    private boolean completedAllRenders = false;
    private Color backgroundColor = Color.WHITE;

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        frameRate(10);
        int minPos = -5, maxPos = 5;
        rotationPeriod = PI * 2 / (float) targetFrames;

//        for (int i = 0; i < 2; i++) {
//            objectsInScene.add(new Sphere(
//                    new Vector3f(random(minPos, maxPos), random(minPos, maxPos), random(minPos, maxPos)),
//                    new Color(random(0, 1), random(0, 1), random(0, 1)),
//                    random(.2f, 1.5f), random(.1f, .9f)));
//        }

        objectsInScene.add(new Sphere(
                new Vector3f(-8, 0, -8),
                new Color(0f, 0f, 1f),
                5f, .1f, .5f));
        objectsInScene.add(new Sphere(
                new Vector3f(8, 0, 8),
                new Color(0f, 1f, 0f),
                5f, .1f, .5f));
        objectsInScene.add(new Sphere(
                new Vector3f(-8, 0, 8),
                new Color(1f, 0f, 0f),
                5f, .1f, .5f));
        objectsInScene.add(new Sphere(
                new Vector3f(8, 0, -8),
                new Color(1f, 1f, 1f),
                5f, .1f, .5f));
        objectsInScene.add(new Sphere(
                new Vector3f(0, -6, 0),
                new Color(.2f, .2f, .2f),
                5f, .02f, .5f));

        objectsInScene.add(new Bulb(
                new Vector3f(),
                .8f,
                30,
                rotationPeriod,
                0.000001f,
                new Color(0, 0, 255),
                new Color(100, 0, 100),
//                Color.WHITE,
//                Color.BLACK,
                Color.BLACK,
                .1f));

        camera = new Camera(
                new Vector3f(0, 0, 3),
                new Vector3f(0, 0, -1),
                new Vector3f(0, 1, 0),
                .00f, width, height);

        lightSources.add(new Sphere(
                new Vector3f(0, 30, 0),
                Color.white,
                1, 1f, .8f));
//        lightSources.add(new Sphere(
//                new Vector3f(0, 30, 10),
//                new Color(255, 200, 200),
//                1, 1f, .8f));
//        lightSources.add(new Sphere(
//                new Vector3f(10 * sin(2 * PI / 3), 30, 10 * cos(2 * PI / 3)),
//                new Color(200, 255, 200),
//                1, 1f, .8f));
//        lightSources.add(new Sphere(
//                new Vector3f(10 * sin(4 * PI / 3), 30, 10 * cos(4 * PI / 3)),
//                new Color(200, 200, 255),
//                1, 1f, .8f));
        objectsInScene.addAll(lightSources);
    }

    @Override
    public void draw() {
        //Rotate the camera around the origin
        Vector3f cameraPos = camera.getPosition();
        cameraPos.set(sin(timeIndex * rotationPeriod) * cameraDistance, 0, cos(timeIndex * rotationPeriod) * cameraDistance);
        camera.setPosition(cameraPos);
        cameraPos.scale(-1);
        cameraPos.normalize();
        camera.setDirection(cameraPos);

        if (timeIndex >= targetFrames) {
            if (!completedAllRenders) {
                movieConverter.convertFramesToMovie(width, height);
                completedAllRenders = true;
            }

            replayNextFrame();
        } else
            renderNextFrame();


    }

    private void renderNextFrame() {
        //Redraws the background if we have either finished a frame or need to restart
        if (shouldRestartCurrentFrame) {
            background(0);
            scanningIndexX = 0;
            scanningIndexY = 0;
            shouldRestartCurrentFrame = false;
        }

        //Preps a member var called pixels
        loadPixels();

        //drawFrameSingleThreaded();
        drawFrameMultiThreaded();

        //Saves the pixels back into PApplet's internal buffer
        updatePixels();

        scanningIndexX++;
        if (scanningIndexX > SCANNING_INDEX_CAP) {
            scanningIndexX = scanningIndexX % SCANNING_INDEX_CAP;
            scanningIndexY++;
        }
        //If we rendered all of our scan lines save the frame and advance to the next one
        if (scanningIndexY > SCANNING_INDEX_CAP) {
            scanningIndexY = scanningIndexY % SCANNING_INDEX_CAP;
            timeIndex++;
            shouldRestartCurrentFrame = true;

            //For long renders, saving every frame in memory is a good way to run out of memory.
            if (targetFrames <= 100) {
                frameCaps.add(screenCap());
            }

            frameTimings.add(System.currentTimeMillis() - frameRenderStartTime);
            saveFrame(String.format(Locale.US, "frames/frame-%06d.png", timeIndex));
            printRenderStats();

            frameRenderStartTime = System.currentTimeMillis();
        }
    }

    private void drawFrameSingleThreaded() {
        Vector3f position = new Vector3f();
        Vector3f direction = new Vector3f();

        for (int y = scanningIndexY; y < height; y += SCANNING_INDEX_CAP) {
            for (int x = scanningIndexX; x < width; x += SCANNING_INDEX_CAP) {
                if (shouldRestartCurrentFrame) break;
                camera.getPixelPosition(x, y, position);
                camera.getPixelDirection(x, y, direction);

                pixels[x + y * width] = rayMarch(direction, position, 50, .0008f, backgroundColor, lightBounces).getRGB();
            }
            if (shouldRestartCurrentFrame) break;
        }
    }

    private void drawFrameMultiThreaded() {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(4);

        int numThreads = (int) Math.ceil(height / (float) SCANNING_INDEX_CAP);
        List<Runnable> drawTasks = new ArrayList<>();
        for (int y = scanningIndexY; y < height; y += SCANNING_INDEX_CAP) {
            try {
                final int threadY = y;
                drawTasks.add(() -> {
                    Vector3f position = new Vector3f();
                    Vector3f direction = new Vector3f();

                    for (int x = scanningIndexX; x < width; x += SCANNING_INDEX_CAP) {
                        if (shouldRestartCurrentFrame) break;
                        camera.getPixelPosition(x, threadY, position);
                        camera.getPixelDirection(x, threadY, direction);

                        pixels[x + threadY * width] = rayMarch(direction, position, 50, .0003f, backgroundColor, lightBounces).getRGB();
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
    }

    private void replayNextFrame() {
        //We can't replay frames if it was a long render, and we didn't store them in memory
        if (timeIndex == frameCaps.size() - 1 && frameCaps.get(imageIndex) != null) {
            image(frameCaps.get(imageIndex), 0, 0);
            imageIndex++;
            if (imageIndex >= frameCaps.size()) imageIndex = 0;
        }
    }

    private void printRenderStats() {
        long totalMilliseconds = 0;
        for (Long time : frameTimings)
            totalMilliseconds += time;
        long averageMSPerFrame = totalMilliseconds / frameTimings.size();

        int secondsToComplete = (int) ((targetFrames - timeIndex) * averageMSPerFrame / 1000);

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
                timeIndex + 1, targetFrames,
                (timeIndex + 1) / (float) targetFrames * 100,
                completionHours, completionMinutes, completionSeconds,
                elapsedHours, elapsedMinutes, elapsedSeconds,
                frameTimings.get(frameTimings.size() - 1) / 1000);
    }

    private PImage screenCap() {
        PImage newImage = createImage(width, height, RGB);
        newImage.loadPixels();
        loadPixels();
        System.arraycopy(pixels, 0, newImage.pixels, 0, pixels.length);
        newImage.updatePixels();
        return newImage;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == UP) {
            Vector3f cameraPos = camera.getPosition();
            Vector3f cameraDir = camera.getDirection();
            float magnitude = cameraPos.length() * 0.05f;
            cameraDistance -= magnitude;
            shouldRestartCurrentFrame = true;
        }
        if (event.getKeyCode() == DOWN) {
            Vector3f cameraPos = camera.getPosition();
            Vector3f cameraDir = camera.getDirection();
            float magnitude = cameraPos.length() * -0.05f;
            cameraDistance += magnitude;
            shouldRestartCurrentFrame = true;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private Color rayMarch(Vector3f direction, Vector3f position, float maxDistance, float distanceBuffer, Color backgroundColor, int numBounces) {
        Color color = backgroundColor;
        Vector3f start = new Vector3f(position);
        int stepNum = 0;
        float cumulativeGlowIntensity = 0;
        Color glowColor = null;

        while (dist(start.x, start.y, start.z,
                position.x, position.y, position.z) < maxDistance && color == backgroundColor) {
            DistanceCalculation closestInfo = closestDistanceTo(position, distanceBuffer);
            Drawable closestDrawable = objectsInScene.get(closestInfo.index);

            if (closestInfo.distance < distanceBuffer) {
                Vector3f normal = closestDrawable.getNormalAtSurface(timeIndex, position);
                float[] rawColor = closestDrawable.getColor(timeIndex, position).getColorComponents(null);

                float ambient = .5f - constrain((stepNum - 10) / 80f, 0, 1f)/2.25f;
                //System.out.printf("ambient: %f steps: %d\n", ambient, stepNum);
                float[] lightColor = getLightColor(position, normal, closestInfo.index, distanceBuffer, numBounces - 1).getColorComponents(null);
                float[] reflectionColor = getReflectionColor(position, normal, direction, closestInfo.index, distanceBuffer, numBounces - 1, backgroundColor).getColorComponents(null);

                float reflectivity = closestDrawable.getReflectivity();

                for (int i = 0; i < rawColor.length; i++) {
                    rawColor[i] = rawColor[i] * reflectivity + reflectionColor[i] * (1 - reflectivity);
                    rawColor[i] = rawColor[i] * lightColor[i] + (ambient * rawColor[i]);
                    if (rawColor[i] < 0) rawColor[i] = 0;
                    if (rawColor[i] > 1) rawColor[i] = 1;
                }

                color = new Color(rawColor[0], rawColor[1], rawColor[2]);
            } else {
                direction.normalize();
                direction.scale(closestInfo.distance * .8f);
                position.add(direction);
            }

            float intensityWithDecay = (float) (closestDrawable.getBaseGlowIntensity() * Math.pow(.5f, closestInfo.distance / glowHalfDistance));
            cumulativeGlowIntensity += intensityWithDecay;
            if (glowColor == null) {
                glowColor = closestDrawable.getGlowColor();
            } else {
                float glowEffectScalar = intensityWithDecay / cumulativeGlowIntensity;
                glowColor = interpolate(glowColor, closestDrawable.getGlowColor(), glowEffectScalar);
            }


            stepNum++;
        }

        if (glowColor != null) {
            //cumulativeGlowIntensity += (random.nextFloat() * 2 - 1);
            float bloomIntensity = constrain(cumulativeGlowIntensity / 60, 0f, 1f);
            //System.out.printf("totalGlow: %f intensity: %f steps: %d\n", cumulativeGlowIntensity, bloomIntensity, stepNum);
            color = interpolate(color, glowColor, bloomIntensity);
        }

        return color;
    }

    private Color getReflectionColor(Vector3f position, Vector3f normal, Vector3f incomingDirection, int sphereIndex, float distanceBuffer, int numBounces, Color backgroundColor) {
        if (numBounces <= 0)
            return backgroundColor;

        Vector3f reflectionDirection = new Vector3f(incomingDirection);
        reflectionDirection.negate();
        reflectionDirection.normalize();
        Vector3f normalDiff = new Vector3f(reflectionDirection);
        normalDiff.sub(normal);
        reflectionDirection.set(normal);
        reflectionDirection.sub(normalDiff);
        reflectionDirection.normalize();

        Vector3f rayPosition = new Vector3f(position);
        reflectionDirection.scale(distanceBuffer * 30);
        rayPosition.add(reflectionDirection);
        reflectionDirection.normalize();

        return rayMarch(reflectionDirection, rayPosition, 50, distanceBuffer, backgroundColor, numBounces);
    }

    private Color getLightColor(Vector3f position, Vector3f normal, int sphereIndex, float distanceBuffer, int numBounces) {
        List<Color> lightColors = new ArrayList<>();

        for (Sphere lightSource : lightSources) {
            if (lightSource == objectsInScene.get(sphereIndex)) {
                lightColors.clear();
                lightColors.add(lightSource.getColor(timeIndex, position));
                break;
            }

            Vector3f reflectionDirection = new Vector3f(lightSource.getPosition());
            reflectionDirection.sub(position);
            reflectionDirection.normalize();

            Vector3f rayPosition = new Vector3f(position);

            float brightness = reflectionDirection.dot(normal);
            if (brightness < 0) brightness = 0;

            Color lightColor;
            if (brightness > 0) {
                reflectionDirection.scale(distanceBuffer * 30);
                rayPosition.add(reflectionDirection);
                reflectionDirection.normalize();

                Color result;
                if (numBounces > 0)
                    result = rayMarch(reflectionDirection, rayPosition, 50, distanceBuffer, Color.BLACK, 0);
                else
                    result = objectsInScene.get(sphereIndex).getColor(timeIndex, rayPosition);

                if (lightSource.distanceToSurface(timeIndex, rayPosition) <= distanceBuffer)
                    lightColor = result;
                else {
                    lightColor = Color.black;
                    brightness = 0;
                }
            } else
                lightColor = Color.black;


            lightColors.add(new Color(
                    (int) (lightColor.getRed() * brightness),
                    (int) (lightColor.getGreen() * brightness),
                    (int) (lightColor.getBlue() * brightness)));
        }
        int[] components = new int[3];
        for (Color lightColor : lightColors) {
            components[0] += lightColor.getRed();
            components[1] += lightColor.getGreen();
            components[2] += lightColor.getBlue();
        }

        return new Color(
                constrain(components[0] / 255.0f, 0, 1),
                constrain(components[1] / 255.0f, 0, 1),
                constrain(components[2] / 255.0f, 0, 1));
    }

    private DistanceCalculation closestDistanceTo(Vector3f position, float distanceBuffer) {
        assert distanceBuffer > 0;

        float min = 100000000;
        int closestIndex = 0;
        for (int sphereIndex = 0; sphereIndex < objectsInScene.size(); sphereIndex++) {
            float distance;
            if ((distance = objectsInScene.get(sphereIndex).distanceToSurface(timeIndex, position)) < min) {
                min = distance;
                closestIndex = sphereIndex;
            }

        }

        return new DistanceCalculation(min, closestIndex);
    }

    private static class DistanceCalculation {
        private float distance;
        private int index;

        public DistanceCalculation(float distance, int index) {
            this.distance = distance;
            this.index = index;
        }
    }

    public static float sigmoid(float in) {
        return 1 / (1 + pow(2.71828f, -1f * in));
    }

    public static Color interpolate(Color startColor, Color endColor, float scalar) {
        float[] startComponents = startColor.getColorComponents(new float[3]);
        float[] endComponents = endColor.getColorComponents(new float[3]);

        float redDiff = endComponents[0] - startComponents[0];
        float greenDiff = endComponents[1] - startComponents[1];
        float blueDiff = endComponents[2] - startComponents[2];

        return new Color(
                startComponents[0] + (redDiff * scalar),
                startComponents[1] + (greenDiff * scalar),
                startComponents[2] + (blueDiff * scalar)
        );
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[]{Renderer.class.getName()};
        PApplet.main(appletArgs);
    }
}

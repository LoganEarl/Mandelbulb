package scenes;


import processing.core.PApplet;
import processing.event.KeyEvent;
import renderer.*;
import settings.CameraSettings;
import settings.RayEngineSettings;
import shape.Bulb;
import shape.Sphere;
import utils.ColorUtils;
import utils.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovingCameraScene extends Scene implements FrameCalculator.FrameCompleteCallback {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int TARGET_TIME_INDEX = 900;
    private static final boolean MANUAL_MODE = false;
    private FrameCalculator frameCalculator;
    private int timeIndex = 0;
    private Camera camera;
    private CameraController cameraController;

    public MovingCameraScene() {
        super(WIDTH, HEIGHT);
    }

    public static void main(String[] args) {
        Scene s = new MovingCameraScene();
        PApplet.main(s.getArgs());
    }

    @Override
    public void setup() {
        float collisionDistance = 0.0005f;
        List<Drawable> lights = Arrays.asList(
                new Sphere(
                        new Vector3(0f, 30f, 0f),
                        ColorUtils.WHITE,
                        2, .5f, .8f
                ),
                new Sphere(
                        new Vector3(4f, 0f, -4f),
                        ColorUtils.BLUE,
                        2, 0.5f, 0.8f
                ),
                new Sphere(
                        new Vector3(4f, 0f, 4f),
                        ColorUtils.GREEN,
                        2f, .5f, 0.8f
                ),
                new Sphere(
                        new Vector3(-4f, 0f, -4f),
                        ColorUtils.RED,
                        2f, .5f, 0.8f
                ));

        List<Drawable> drawables = new ArrayList<>(Arrays.asList(
                Bulb.builder()
                        .position(new Vector3(0f, 0f, 0f))
                        .reflectivity(.2f)
                        .steps(30)
                        .period((float) Math.PI / (float)TARGET_TIME_INDEX)
                        .granularity(collisionDistance)
                        .startColor(ColorUtils.WHITE)
                        .endColor(ColorUtils.WHITE)
                        .glowColor(color(0, 0, 0, 255))
                        .glowIntensity(.0f)
                        .build(),
                new Sphere(
                        new Vector3(-4f, 0f, 4f),
                        ColorUtils.WHITE,
                        2, .5f, .8f
                )
        ));
        drawables.addAll(lights);

        camera = new UvCamera(new CameraSettings(
                new Vector3(2, 0, 0),
                new Vector3(-1, 0, 0),
                new Vector3(0, 1, 0),
                width, height, (float) Math.PI / 2));

        RayEngine rayEngine = new RayMarchEngine(drawables, lights,
                RayEngineSettings.defaultEngineSettings()
                        .recursiveSteps(4)
                        .minCollisionDistance(collisionDistance)
                        .maxCollisionDistance(collisionDistance)
                        .minAmbientLevel(0)
                        .maxAmbientLevel(0.2f)
                        .minAmbientSteps(5)
                        .maxAmbientSteps(30)
                        .glowHalfDistance(2f)
                        .build()
        );

        loadPixels();
//        frameCalculator = SerialScatteredFrameCalculator.builder()
        frameCalculator = ParallelScatteredFrameCalculator.builder()
                .callback(this)
                .camera(camera)
                .rayEngine(rayEngine)
                .height(height)
                .width(width)
                .pixelBuffer(pixels)
                .build();

        cameraController = new CameraController(camera, frameCalculator);
    }

    @Override
    public void draw() {
        frameCalculator.update(timeIndex);
        updatePixels();
    }

    @Override
    public void onFrameComplete(int[] pixels) {
        updatePixels();
        if (!MANUAL_MODE) {
            //we can ignore the pixel array here. We fed it the reference to our internal one which it is making modifications to
            saveFrame(timeIndex);
            frameCalculator.printFrameRenderStats(timeIndex, TARGET_TIME_INDEX);

            timeIndex++;
            if (timeIndex > TARGET_TIME_INDEX) {
                exit();
            }

            float cameraRotationRadians = timeIndex / (float) (TARGET_TIME_INDEX) * PApplet.PI * 2f;
            float cameraX = cos(cameraRotationRadians) * 2;
            float cameraZ = sin(cameraRotationRadians) * 2;

            camera.setPosition(camera.getPosition()
                    .setV(cameraX, 0, cameraZ));

            camera.setDirection(camera.getPosition().clone()
                    .negateV()
                    .normalizeV());
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (MANUAL_MODE) {
            cameraController.keyReleased(event.getKey());
        }
    }
}

package scenes;


import processing.core.PApplet;
import processing.event.KeyEvent;
import renderer.*;
import settings.CameraSettings;
import settings.RayEngineSettings;
import shape.RepeatingShape;
import shape.Sphere;
import utils.ColorUtils;
import utils.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LightingTest extends Scene implements FrameCalculator.FrameCompleteCallback {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int TARGET_TIME_INDEX = 20;
    private static final boolean MANUAL_MODE = true;
    private FrameCalculator frameCalculator;
    private int timeIndex = 0;
    private Camera camera;
    private CameraController cameraController;

    public LightingTest() {
        super(WIDTH, HEIGHT);
    }

    public static void main(String[] args) {
        Scene s = new LightingTest();
        PApplet.main(s.getArgs());
    }

    @Override
    public void setup() {
        List<Drawable> lights = Arrays.asList(new Sphere(
                new Vector3(30, 30, 0),
                ColorUtils.WHITE,
                1, 1f, .8f));

        List<Drawable> drawables = new ArrayList<>(Arrays.asList(
                new RepeatingShape(20, new Sphere(
                        new Vector3(-2f, 0f, 2f),
                        ColorUtils.BLUE,
                        0.5f, 0.5f, 0f
                )),
                new Sphere(
                        new Vector3(2f, 0f, -2f),
                        ColorUtils.WHITE,
                        1, 0.9f, 0f
                ),
                new Sphere(
                        new Vector3(2f, 0f, 2f),
                        ColorUtils.GREEN,
                        2f, .1f, 0f
                ),
                new Sphere(
                        new Vector3(-2f, 0f, -2f),
                        ColorUtils.RED,
                        2f, .5f, 0f
                )
        ));
        drawables.addAll(lights);

        camera = new UvCamera(new CameraSettings(
                new Vector3(0, 0, 0),
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                width, height, (float) Math.PI / 2));

        RayEngine rayEngine = new RayMarchEngine(drawables, lights,
                RayEngineSettings.defaultEngineSettings()
                        .recursiveSteps(6)
                        .build()
        );

        loadPixels();
        frameCalculator = SerialScatteredFrameCalculator.builder()
//        frameCalculator = ParallelScatteredFrameCalculator.builder()
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
        if (!MANUAL_MODE) {
            //we can ignore the pixel array here. We fed it the reference to our internal one which it is making modifications to
            saveFrame(timeIndex);
            frameCalculator.printFrameRenderStats(timeIndex, TARGET_TIME_INDEX);

            timeIndex++;
            if (timeIndex > TARGET_TIME_INDEX) {
                exit();
            }

            float cameraRotationRadians = timeIndex / (float) (TARGET_TIME_INDEX) * PApplet.PI * 2f;
            camera.setDirection(new Vector3(
                    PApplet.cos(cameraRotationRadians),
                    0,
                    PApplet.sin(cameraRotationRadians)
            ));
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (MANUAL_MODE) {
            cameraController.keyReleased(event.getKey());
        }
    }
}

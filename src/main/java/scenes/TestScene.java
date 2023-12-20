package scenes;

import com.fasterxml.jackson.core.JsonProcessingException;
import processing.core.PApplet;
import processing.event.KeyEvent;
import renderer.*;
import settings.CameraSettings;
import settings.RayEngineSettings;
import settings.RenderSettings;
import shape.Bulb;
import shape.RepeatingShape;
import shape.Sphere;
import utils.ColorUtils;
import utils.JsonUtils;
import utils.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestScene extends Scene implements FrameCalculator.FrameCompleteCallback {
    //16k portrait
//    private static final int HEIGHT = 15360;
//    private static final int WIDTH = 8640;
    //16k
    private static final int WIDTH = 15360;
    private static final int HEIGHT = 8640;
    //4k
//    private static final int WIDTH = 3840;
//    private static final int HEIGHT = 2160;
    //1k
//    private static final int WIDTH = 1920;
//    private static final int HEIGHT = 1080;
    private static final int WIDTH_LOW_RES = 600;
    private static final int HEIGHT_LOW_RES = 400;
    private static final boolean MANUAL_MODE = true;

    private FrameCalculator frameCalculator;
    private CameraController cameraController;

    public TestScene() {
        super(MANUAL_MODE ? WIDTH_LOW_RES : WIDTH, MANUAL_MODE ? HEIGHT_LOW_RES : HEIGHT);
    }

    public static void main(String[] args) {
        Scene s = new TestScene();
        PApplet.main(s.getArgs());
    }

    @Override
    public void setup() {
        background(255);
        loadPixels();

        List<Drawable> lights = Arrays.asList(
                new Sphere(
                        new Vector3(0, 4f, -4),
                        ColorUtils.WHITE,
                        .1f, 1f, .8f
                )
        );

        int bulbSteps = 120;
        float minCollision = 0.000001f;
        float bulbGranularity = minCollision;
        float maxCollision = 0.003f;
        float renderDistance = 3000;
//        float minCollision = 0.000001f;
//        float maxCollision = 0.003f;
//        float renderDistance = 3000;

        if (MANUAL_MODE) {
            bulbSteps = 90;
            bulbGranularity = 0.0001f;
            minCollision = 0.0001f;
            maxCollision = 0.001f;
            renderDistance = 50;
        }

        Bulb bulb = Bulb.builder()
                .position(new Vector3(0f, 0f, 0f))
                .reflectivity(1f)
                .steps(bulbSteps)
                .period((float) Math.PI * 2f)
                .granularity(bulbGranularity)
                .startColor(ColorUtils.WHITE)
                .endColor(ColorUtils.WHITE)
                .glowColor(color(0, 0, 120, 255))
                .glowIntensity(.0f)
                .build();

        List<Drawable> drawables = new ArrayList<>(Arrays.asList(
                new RepeatingShape(10f, bulb),
//                bulb,
//                new InvertedShape(bulb),
                lights.get(0)
        ));


        Camera camera = new UvCamera(new CameraSettings(
                new Vector3(0f, 0f, 4),
                new Vector3(0, 0, -4f),
                new Vector3(0, 1f, 0),
                width, height, (float) Math.PI / 2
        ));

        RayEngine rayEngine = new RayMarchEngine(
                drawables, lights,
                RayEngineSettings.defaultEngineSettings()
                        .backgroundColor(ColorUtils.GRAY)
                        .recursiveSteps(1)
                        .glowHalfDistance(.1f)
                        .maxRenderDistance(renderDistance)
                        .minAmbientLevel(1f)
                        .maxAmbientLevel(0f)
                        .minAmbientSteps(0)
                        .maxAmbientSteps(100)
                        .minCollisionDistance(minCollision)
                        .maxCollisionDistance(maxCollision)
                        .build());

        frameCalculator = SerialScatteredFrameCalculator.builder()
//        frameCalculator = ParallelScatteredFrameCalculator.builder()
                .callback(this)
                .camera(camera)
                .rayEngine(rayEngine)
                .height(height)
                .width(width)
                .pixelBuffer(pixels)
                .build();

        frameCalculator.setFrameLogEnabled(!MANUAL_MODE);

        cameraController = new CameraController(camera, frameCalculator);

        RenderSettings settings = RenderSettings.builder()
                .cameraSettings(camera.getSettings())
                .rayEngineSettings(rayEngine.getSettings())
                .screenWidth(width)
                .screenHeight(height)
                .objects(drawables)
                .lights(lights)
                .build();

        camera.setPosition(new Vector3(0.945169f, 0.475003f, 0.810790f));
        camera.setUpVector(new Vector3(0.451098f, 0.881921f, -0.136840f));
        camera.setDirection(new Vector3(-0.710235f, 0.261894f, -0.653436f));

        try {
            System.out.println(JsonUtils.getObjectMapperInstance().writeValueAsString(settings));
            System.out.println();
        } catch (JsonProcessingException ignored) {
        }
    }

    @Override
    public void draw() {
        frameCalculator.update(1);
        updatePixels();
    }

    @Override
    public void onFrameComplete(int[] pixels) {
        updatePixels();
        if (!MANUAL_MODE) {
            System.out.println("Finished rendering image, saving frame");
            saveFrame(1);

            System.out.println("Finished saving, exiting");
            exit();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (MANUAL_MODE) {
            cameraController.keyReleased(event.getKey());
        }
    }

}

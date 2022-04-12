package scenes;


import processing.core.PApplet;
import renderer.*;
import shape.Sphere;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LightingTest extends Scene implements FrameCalculator.FrameCompleteCallback {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int TARGET_TIME_INDEX = 20;

    private FrameCalculator frameCalculator;
    private int timeIndex = 0;

    public LightingTest() {
        super(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        List<Drawable> lights = List.of(new Sphere(
                new Vector3f(30, 30, 0),
                Color.white,
                1, 1f, .8f));

        List<Drawable> drawables = new ArrayList<>(List.of(
                new Sphere(
                        new Vector3f(0f, 2f, 0f),
                        Color.BLUE,
                        0.5f, 0.5f, .5f
                ),
                new Sphere(
                        new Vector3f(0f, 0f, 0f),
                        Color.WHITE,
                        1, 0.5f, .5f
                ),
                new Sphere(
                        new Vector3f(0f, -4f, 0f),
                        Color.GREEN,
                        2f, .5f, .5f
                )
        ));
        drawables.addAll(lights);

        Camera camera = new Camera(
                new Vector3f(0, 0, 10),
                new Vector3f(0, 0, -1),
                new Vector3f(0, 1, 0),
                .00f, width, height);

        RayEngine rayEngine = RayMarchEngine.builder()
                .backgroundColor(Color.BLACK)
                .collisionDistance(.0003f)
                .glowHalfDistance(0.1f)
                .maxRenderDistance(100f)
                .recursiveSteps(2)
                .lightSources(lights)
                .objectsInScene(drawables)
                .build();

        loadPixels();
        frameCalculator = SerialScatteredFrameCalculator.builder()
                .callback(this)
                .camera(camera)
                .rayEngine(rayEngine)
                .height(height)
                .width(width)
                .pixelBuffer(pixels)
                .build();
    }

    @Override
    public void draw() {
        frameCalculator.update(timeIndex);
        updatePixels();
    }

    @Override
    public void onFrameComplete(int[] pixels) {
        //we can ignore the pixel array here. We fed it the reference to our internal one which it is making modifications to
        saveFrame(timeIndex);
        frameCalculator.printRenderStats(timeIndex, TARGET_TIME_INDEX);
        timeIndex++;
        timeIndex = timeIndex % TARGET_TIME_INDEX;
    }

    public static void main(String[] args){
        Scene s = new LightingTest();
        PApplet.main(s.getArgs());
    }
}

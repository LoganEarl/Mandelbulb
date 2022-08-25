package scenes;

import processing.core.PApplet;
import renderer.*;
import shape.Bulb;
import shape.RepeatingShape;
import shape.Sphere;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfiniteBulb extends Scene implements FrameCalculator.FrameCompleteCallback {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int TARGET_TIME_INDEX = 1;

    private FrameCalculator frameCalculator;
    private int timeIndex = 0;

    public InfiniteBulb() {
        super(WIDTH, HEIGHT);
    }

    @Override
    public void setup() {
        List<Drawable> lights = Arrays.asList(
                new Sphere(
                        new Vector3f(0, 4f, 0),
                        Color.white,
                        .1f, 1f, .8f
                )
        );

        List<Drawable> drawables = new ArrayList<>(Arrays.asList(
                new RepeatingShape(10f, new Bulb(
                        new Vector3f(0f, 0f, 0f),
                        1f,
                        60,
                        (float) Math.PI * 2f,
                        0.001f,
                        Color.BLACK,
                        Color.WHITE,
                        Color.WHITE,
                        0f
                ))
        ));

        Camera camera = new Camera(
                new Vector3f(0, 0, 4),
                new Vector3f(0, 0, -1),
                new Vector3f(0, 1, 0),
                .00f, width, height);

        RayEngine rayEngine = RayMarchEngine.defaultEngineSettings(drawables, lights)
                .backgroundColor(new Color(180,180,180))
                .recursiveSteps(1)
                .glowHalfDistance(.4f)
                .maxRenderDistance(3000f)
                .minAmbientLevel(0)
                .maxAmbientLevel(1f)
                .minAmbientSteps(0)
                .maxAmbientSteps(100)
                .build();

        background(255);
        loadPixels();
        //frameCalculator = SerialScatteredFrameCalculator.builder()
        frameCalculator = ParallelScatteredFrameCalculator.builder()
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
        saveFrame(timeIndex);
        frameCalculator.printRenderStats(timeIndex, TARGET_TIME_INDEX);
        if(timeIndex < TARGET_TIME_INDEX) timeIndex++;
    }

    public static void main(String[] args){
        Scene s = new InfiniteBulb();
        PApplet.main(s.getArgs());
    }
}

import com.fasterxml.jackson.databind.ObjectMapper;
import renderer.*;
import settings.CameraSettings;
import settings.RayEngineSettings;
import settings.RenderSettings;
import shape.RepeatingShape;
import shape.Sphere;
import utils.ColorUtils;
import utils.JsonUtils;
import utils.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeadlessFrameRenderer implements FrameCalculator.FrameCompleteCallback {
    public static void main(String... args) throws Exception {
        int width = 600;
        int height = 400;

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

        Camera camera = new UvCamera(new CameraSettings(
                new Vector3(0, 0, 0),
                new Vector3(1, 0, 0),
                new Vector3(0, 1, 0),
                width, height, (float) Math.PI / 2));

        RayEngine rayEngine = new RayMarchEngine(drawables, lights,
                RayEngineSettings.defaultEngineSettings()
                        .recursiveSteps(6)
                        .build()
        );

        RenderSettings settings = RenderSettings.builder()
                .cameraSettings(camera.getSettings())
                .rayEngineSettings(rayEngine.getSettings())
                .screenWidth(width)
                .screenHeight(height)
                .objects(drawables)
                .lights(lights)
                .build();

        ObjectMapper mapper = JsonUtils.getObjectMapperInstance();
        String json = mapper.writeValueAsString(settings);

        RenderSettings parsed = mapper.readValue(json, RenderSettings.class);
        String otherJson = mapper.writeValueAsString(parsed);
        assert json.equals(otherJson);
    }

    @Override
    public void onFrameComplete(int[] pixels) {
        //Do nothing for now

    }
}

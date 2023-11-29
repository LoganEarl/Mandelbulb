package settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import renderer.Drawable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RenderSettings {
    private int screenWidth;
    private int screenHeight;
    private CameraSettings cameraSettings;
    private RayEngineSettings rayEngineSettings;
    private List<Drawable> lights;
    private List<Drawable> objects;
}

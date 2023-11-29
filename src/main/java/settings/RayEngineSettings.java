package settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.ColorUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RayEngineSettings {
    private float minCollisionDistance;
    private float maxCollisionDistance;
    private int backgroundColor;
    private int recursiveSteps;
    private float maxRenderDistance;
    private float glowHalfDistance;
    private float minAmbientLevel;
    private float maxAmbientLevel;
    private int minAmbientSteps;
    private int maxAmbientSteps;

    public static RayEngineSettings.RayEngineSettingsBuilder defaultEngineSettings() {
        return RayEngineSettings.builder()
                .backgroundColor(ColorUtils.BLACK)
                .minCollisionDistance(.000001f)
                .maxCollisionDistance(.001f)
                .glowHalfDistance(0.1f)
                .maxRenderDistance(100f)
                .recursiveSteps(2)
                .minAmbientLevel(0)
                .maxAmbientLevel(.5f)
                .minAmbientSteps(0)
                .maxAmbientSteps(20);
    }
}

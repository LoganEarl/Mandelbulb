package renderer;

import lombok.Builder;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static math.Utils.*;

@Builder
public class RayMarchEngine implements RayEngine {
    private List<Drawable> objectsInScene;
    private List<Drawable> lightSources;

    private float collisionDistance;
    private Color backgroundColor;
    private int recursiveSteps;
    private float maxRenderDistance;
    private float glowHalfDistance;

    public static RayMarchEngine.RayMarchEngineBuilder defaultEngineSettings(List<Drawable> drawables, List<Drawable> lights){
        return RayMarchEngine.builder()
                .backgroundColor(Color.BLACK)
                .collisionDistance(.0003f)
                .glowHalfDistance(0.1f)
                .maxRenderDistance(100f)
                .recursiveSteps(2)
                .lightSources(lights)
                .objectsInScene(drawables);
    }

    public Color calculateRay(Vector3f direction, Vector3f position, int timeIndex){
        return rayMarch(direction, position, timeIndex, recursiveSteps);
    }

    private Color rayMarch(Vector3f direction, Vector3f position, int timeIndex, int remainingRecursiveSteps) {
        Color color = backgroundColor;
        Vector3f start = new Vector3f(position);
        int stepNum = 0;
        float cumulativeGlowIntensity = 0;
        Color glowColor = null;

        while (dist(start.x, start.y, start.z,
                position.x, position.y, position.z) < maxRenderDistance && color == backgroundColor) {
            DistanceCalculation closestInfo = closestDistanceTo(position, timeIndex);
            Drawable closestDrawable = objectsInScene.get(closestInfo.index);

            if (closestInfo.distance < collisionDistance) {
                Vector3f normal = closestDrawable.getNormalAtSurface(timeIndex, position, start);
                float[] rawColor = closestDrawable.getColor(timeIndex, position).getColorComponents(null);

                float ambient = .5f - constrain((stepNum - 10) / 80f, 0, 1f)/2.25f;
                //System.out.printf("ambient: %f steps: %d\n", ambient, stepNum);
                float[] lightColor = getLightColor(position, normal, closestInfo.index, timeIndex, remainingRecursiveSteps - 1).getColorComponents(null);
                float[] reflectionColor = getReflectionColor(position, normal, direction, timeIndex, remainingRecursiveSteps - 1).getColorComponents(null);

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
                direction.scale(closestInfo.distance);
                position.add(direction);
            }

            float intensityWithDecay = (float) (closestDrawable.getBaseGlowIntensity() * Math.pow(.5f, closestInfo.distance / glowHalfDistance));
            cumulativeGlowIntensity += intensityWithDecay;
            if (glowColor == null) {
                glowColor = closestDrawable.getGlowColor();
            } else if(cumulativeGlowIntensity > 0) {
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

    private Color getReflectionColor(Vector3f position, Vector3f normal, Vector3f incomingDirection, int timeIndex, int remainingRecursiveSteps) {
        if (remainingRecursiveSteps <= 0)
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
        reflectionDirection.scale(collisionDistance * 30);
        rayPosition.add(reflectionDirection);
        reflectionDirection.normalize();

        return rayMarch(reflectionDirection, rayPosition, timeIndex, remainingRecursiveSteps - 1);
    }

    private Color getLightColor(Vector3f position, Vector3f normal, int sphereIndex, int timeIndex, int remainingRecursiveSteps) {
        List<Color> lightColors = new ArrayList<>();

        for (Drawable lightSource : lightSources) {
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
                reflectionDirection.scale(collisionDistance * 30);
                rayPosition.add(reflectionDirection);
                reflectionDirection.normalize();

                Color result;
                if (remainingRecursiveSteps > 0)
                    result = rayMarch(reflectionDirection, rayPosition, timeIndex, remainingRecursiveSteps - 1);
                else
                    result = objectsInScene.get(sphereIndex).getColor(timeIndex, rayPosition);

                if (lightSource.distanceToSurface(timeIndex, rayPosition) <= collisionDistance)
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

    private DistanceCalculation closestDistanceTo(Vector3f position, int timeIndex) {
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
        private final float distance;
        private final int index;

        public DistanceCalculation(float distance, int index) {
            this.distance = distance;
            this.index = index;
        }
    }
}

package renderer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;
import settings.RayEngineSettings;
import utils.Utils;
import utils.Vector3;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

import static utils.ColorUtils.*;
import static utils.Utils.*;

@AllArgsConstructor
public class RayMarchEngine implements RayEngine {
    private List<Drawable> objectsInScene;
    private List<Drawable> lightSources;

    @Delegate
    @Getter
    private RayEngineSettings settings;

    public int calculateRay(Vector3 direction, Vector3 position, int timeIndex) {
        return rayMarch(direction, position, timeIndex, getRecursiveSteps());
    }

    private int rayMarch(Vector3 direction, Vector3 position, int timeIndex, int remainingRecursiveSteps) {
        int color = getBackgroundColor();
        Vector3 start = new Vector3(position);
        int stepNum = 0;
        float cumulativeGlowIntensity = 0;
        Integer glowColor = null;

        while (dist(start.x, start.y, start.z,
                position.x, position.y, position.z) < getMaxRenderDistance() && color == getBackgroundColor()) {
            DistanceCalculation closestInfo = closestDistanceTo(position, timeIndex);
            Drawable closestDrawable = objectsInScene.get(closestInfo.index);

            if (closestInfo.distance < getCollisionDistance(position)) {
                Vector3 normal = closestDrawable.getNormalAtSurface(timeIndex, position, start);
                int rawColor = closestDrawable.getColor(timeIndex, position);


                int lightColor = getLightColor(position, direction, normal, closestInfo.index, timeIndex, remainingRecursiveSteps - 1);
                int reflectionColor = getReflectionColor(position, normal, direction, timeIndex, remainingRecursiveSteps - 1);

                float reflectivity = closestDrawable.getReflectivity();
                float ambient = traditionalAmbientModel(stepNum);

                color = applyColorModel(rawColor, reflectionColor, lightColor, reflectivity, ambient);
            } else {
                direction.normalize();
                direction.scale(closestInfo.distance * .9f);
                position.add(direction);
            }

            float intensityWithDecay = (float) (closestDrawable.getBaseGlowIntensity() *
                    Math.pow(.5f, closestInfo.distance / getGlowHalfDistance()));
            cumulativeGlowIntensity += intensityWithDecay;
            if (glowColor == null) {
                glowColor = closestDrawable.getGlowColor();
            } else if (cumulativeGlowIntensity > 0) {
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

    private float sinusoidalAmbientModel(int stepNum) {
        int minAmbientSteps = getMinAmbientSteps();
        int maxAmbientSteps = getMaxAmbientSteps();
        float maxAmbientLevel = getMaxAmbientLevel();
        float minAmbientLevel = getMinAmbientLevel();
        int ambientRange = maxAmbientSteps - minAmbientSteps;
        float percentage = (float) ((Math.sin((((stepNum - minAmbientSteps) - ambientRange / 2f) * Math.PI) / ambientRange)) / 2f + .5);
        return percentage * (maxAmbientLevel - minAmbientLevel) + minAmbientLevel;
    }

    private float piecewiseLinearAmbientModel(int stepNum) {
        int minAmbientSteps = getMinAmbientSteps();
        int maxAmbientSteps = getMaxAmbientSteps();
        float maxAmbientLevel = getMaxAmbientLevel();
        float minAmbientLevel = getMinAmbientLevel();
        int ambientRange = maxAmbientSteps - minAmbientSteps;
        int numWraps = (stepNum - minAmbientSteps) / ambientRange;
        int steps = (stepNum - minAmbientSteps) % ambientRange + minAmbientSteps;
        int min = numWraps % 2 == 1 ? maxAmbientSteps : minAmbientSteps;
        int max = numWraps % 2 == 1 ? minAmbientSteps : maxAmbientSteps;
        float ambientPercentage = (steps - min) / (max - (float) min);
        return ambientPercentage * (maxAmbientLevel - minAmbientLevel) + minAmbientLevel;
    }

    private float traditionalAmbientModel(int stepNum) {
        int minAmbientSteps = getMinAmbientSteps();
        int maxAmbientSteps = getMaxAmbientSteps();
        float maxAmbientLevel = getMaxAmbientLevel();
        float minAmbientLevel = getMinAmbientLevel();
        int ambientSteps = constrainInt(stepNum, minAmbientSteps, maxAmbientSteps);
        float ambientPercentage = (ambientSteps - minAmbientSteps) / (maxAmbientSteps - (float) minAmbientSteps);
        return ambientPercentage * (maxAmbientLevel - minAmbientLevel) + minAmbientLevel;
    }

    private int applyColorModel(int rawColor, int reflectionColor, int lightColor, float reflectivity, float ambient) {
        return colorF(
                applySingleColorModel(redF(rawColor), redF(reflectionColor), redF(lightColor), reflectivity, ambient),
                applySingleColorModel(greenF(rawColor), greenF(reflectionColor), greenF(lightColor), reflectivity, ambient),
                applySingleColorModel(blueF(rawColor), blueF(reflectionColor), blueF(lightColor), reflectivity, ambient),
                1
        );
    }

    private float applySingleColorModel(float raw, float reflection, float light, float reflectivity, float ambient) {
        float component = raw * reflectivity + reflection * (1 - reflectivity);
        component = component * light + ((1 - ambient) * component);
        return constrain(component, 0, 1);
    }

    private float getCollisionDistance(Vector3 position) {
        float maxRenderDistance = getMaxRenderDistance();
        float minCollisionDistance = getMinCollisionDistance();
        float maxCollisionDistance = getMaxCollisionDistance();
        //Its magic
        float distFromOrigin = Utils.constrain(position.length(), 0, maxRenderDistance);
        float interpolationScalar = (float) (-1 * Math.log10((-90 / maxRenderDistance) * (distFromOrigin - maxRenderDistance) + 10) + 2);
        return (maxCollisionDistance - minCollisionDistance) * interpolationScalar + minCollisionDistance;
//        return maxCollisionDistance;
    }

    private int getReflectionColor(Vector3 position, Vector3 normal, Vector3f incomingDirection, int timeIndex, int remainingRecursiveSteps) {
        if (remainingRecursiveSteps <= 0)
            return getBackgroundColor();

        Vector3 reflectionDirection = new Vector3(incomingDirection)
                .negateV()
                .normalizeV();
        Vector3 normalDiff = new Vector3(reflectionDirection)
                .subV(normal);

        reflectionDirection = reflectionDirection.setV(normal)
                .subV(normalDiff)
                .normalizeV();

        Vector3 rayPosition = new Vector3(position)
                .addV(reflectionDirection.scaleV(getCollisionDistance(position) * 30));
        reflectionDirection.normalize();

        return rayMarch(reflectionDirection, rayPosition, timeIndex, remainingRecursiveSteps - 1);
    }

    private int getLightColor(Vector3 position, Vector3 direction, Vector3 normal, int sphereIndex, int timeIndex, int remainingRecursiveSteps) {
        List<Integer> lightColors = new ArrayList<>();

        for (Drawable lightSource : lightSources) {
            if (lightSource == objectsInScene.get(sphereIndex)) {
                lightColors.clear();
                lightColors.add(lightSource.getColor(timeIndex, position));
                break;
            }

            Vector3 reflectionDirection = new Vector3(lightSource.getPosition())
                    .subV(position)
                    .normalizeV();

            Vector3 rayPosition = new Vector3(position);

            float brightness = reflectionDirection.dot(normal);
            if (brightness < 0) brightness = 0;
            float collisionDistance = getCollisionDistance(position);

            int lightColor;
            if (brightness > 0) {
                //Back the ray out by 3x the collision distance the way it came
                rayPosition.add(direction.clone().normalizeV().scaleV(-1 * collisionDistance * 3));

                int result;
                if (remainingRecursiveSteps > 0)
                    result = rayMarch(reflectionDirection, rayPosition, timeIndex, remainingRecursiveSteps - 1);
                else
                    result = objectsInScene.get(sphereIndex).getColor(timeIndex, rayPosition);

                if (lightSource.distanceToSurface(timeIndex, rayPosition) <= getCollisionDistance(rayPosition))
                    lightColor = result;
                else {
                    lightColor = BLACK;
                    brightness = 0;
                }
            } else
                lightColor = BLACK;


            lightColors.add(color(
                    (int) (red(lightColor) * brightness),
                    (int) (blue(lightColor) * brightness),
                    (int) (green(lightColor) * brightness),
                    255));
        }
        int[] components = new int[3];
        for (Integer lightColor : lightColors) {
            components[0] += red(lightColor);
            components[1] += green(lightColor);
            components[2] += blue(lightColor);
        }

        return color(components[0], components[1], components[2], 255);
    }

    private DistanceCalculation closestDistanceTo(Vector3 position, int timeIndex) {
        float min = 100000000;
        int closestIndex = 0;
        for (int sphereIndex = 0; sphereIndex < objectsInScene.size(); sphereIndex++) {
            float distance = objectsInScene.get(sphereIndex).distanceToSurface(timeIndex, position);
            if (distance < min) {
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

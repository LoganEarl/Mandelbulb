package renderer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import shape.InvertedShape;
import utils.Vector3;
import shape.Bulb;
import shape.RepeatingShape;
import shape.Sphere;

import java.awt.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "drawableType")
@JsonSubTypes({
        @JsonSubTypes.Type(value= Bulb.class, name = "BULB"),
        @JsonSubTypes.Type(value= Sphere.class, name = "SPHERE"),
        @JsonSubTypes.Type(value = RepeatingShape.class, name = "REPEATING_SHAPE"),
        @JsonSubTypes.Type(value = InvertedShape.class, name = "INVERTED_SHAPE")
})
public interface Drawable {
    float distanceToSurface(int timeIndex, Vector3 point);
    Vector3 getPosition();
    Vector3 getNormalAtSurface(int timeIndex, Vector3 position, Vector3 origin);
    int getColor(int timeIndex, Vector3 position);
    float getReflectivity();
    float getBaseGlowIntensity();
    int getGlowColor();

    @JsonIgnore
    DrawableType getDrawableType();

    enum DrawableType {
        //Update the annotations above when you change this!!
        BULB, SPHERE, REPEATING_SHAPE, INVERTED_SHAPE
    }
}

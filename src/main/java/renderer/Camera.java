package renderer;

import processing.core.PApplet;
import javax.vecmath.Vector3f;

public class Camera {
    private final Vector3f position;
    private final Vector3f direction;
    private final Vector3f upVector;
    private final Vector3f sideVector;
    private final float verticalSpacing;
    private final float horizontalSpacing;
    private final int screenWidth;
    private final int screenHeight;
    private final float aspectRatio;

    public Camera(Vector3f position, Vector3f direction, Vector3f upVector, float pixelSpacing, int screenWidth, int screenHeight) {
        this.position = position;
        this.direction = direction;
        this.upVector = upVector;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.sideVector = new Vector3f();

        aspectRatio = screenWidth / (float) screenHeight;
        verticalSpacing = pixelSpacing;
        horizontalSpacing = pixelSpacing * aspectRatio;

        updateSideVector();
        upVector.normalize();
    }

    public void getPixelPosition(int x, int y, Vector3f modify) {
        //the amount we shift up down/ left right
        float xChange = (x - screenWidth / 2.0f) * horizontalSpacing;
        float yChange = (y - screenHeight / 2.0f) * verticalSpacing * -1;

        modify.set(position);

        //points to the pixel position
        if(xChange != 0) {
            sideVector.scale(xChange);
            modify.add(sideVector);
            sideVector.scale(xChange);
            sideVector.normalize();
        }
        if(yChange != 0){
            upVector.scale(yChange);
            modify.add(upVector);
            upVector.scale(yChange);
            upVector.normalize();
        }
    }

    public void getPixelDirection(int x, int y, Vector3f modify) {
        modify.set(direction);

        float widthOffsetAngle = x/(float)screenWidth;
        float fov = (float) Math.PI / 4.0f;
        widthOffsetAngle = (widthOffsetAngle * fov - fov /2f) * aspectRatio;

        float heightOffsetAngle = y/(float)screenHeight;
        heightOffsetAngle = heightOffsetAngle * fov - fov /2f;

        rotateVectorCC(modify,upVector,widthOffsetAngle);
        rotateVectorCC(modify, sideVector, heightOffsetAngle);
    }

    public static void rotateVectorCC(Vector3f vec, Vector3f axis, float theta){
        float x, y, z;
        float u, v, w;
        x=vec.getX();y=vec.getY();z=vec.getZ();
        u=axis.getX();v=axis.getY();w=axis.getZ();
        float c = u * x + v * y + w * z;
        float xPrime = u* c *(1f - PApplet.cos(theta))
                + x*PApplet.cos(theta)
                + (-w*y + v*z)*PApplet.sin(theta);
        float yPrime = v* c *(1f - PApplet.cos(theta))
                + y*PApplet.cos(theta)
                + (w*x - u*z)*PApplet.sin(theta);
        float zPrime = w* c *(1f - PApplet.cos(theta))
                + z*PApplet.cos(theta)
                + (-v*x + u*y)*PApplet.sin(theta);
        vec.set(xPrime, yPrime, zPrime);
    }

    private void updateSideVector(){
        sideVector.cross(upVector, direction);
        sideVector.normalize();
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }

    public void setDirection(Vector3f direction) {
        this.direction.set(direction);
        updateSideVector();
    }

    public Vector3f getUpVector() {
        return new Vector3f(upVector);
    }

    public void setUpVector(Vector3f upVector) {
        this.upVector.set(upVector);
        updateSideVector();
    }
}

package settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import utils.Vector3;

@Data
@NoArgsConstructor
public class CameraSettings {
    private Vector3 position;
    private Vector3 direction;
    private Vector3 upVector;
    private Vector3 sideVector;
    private int screenWidth;
    private int screenHeight;
    private float fovRadians;

    public CameraSettings(Vector3 position, Vector3 direction, Vector3 upVector, int screenWidth, int screenHeight, float fovRadians) {
        this.position = position;
        this.direction = direction;
        this.upVector = upVector;
        this.sideVector = new Vector3();
        this.updateSideVector();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.fovRadians = fovRadians;
    }

    private void updateSideVector() {
        if (sideVector != null && upVector != null && direction != null) {
            sideVector.cross(getUpVector(), getDirection());
            sideVector.normalize();
        }
    }

    public void setDirection(Vector3 direction) {
        if (this.direction == null) this.direction = new Vector3();
        this.getDirection().setV(direction)
                .normalizeV();
        updateSideVector();
    }

    public void setUpVector(Vector3 upVector) {
        if (this.upVector == null) this.upVector = new Vector3();
        this.getUpVector().set(upVector);
        updateSideVector();
    }

    public void setPosition(Vector3 position) {
        if (this.position == null) this.position = new Vector3();
        this.getPosition().set(position);
    }
}

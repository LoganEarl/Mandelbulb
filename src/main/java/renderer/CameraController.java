package renderer;

import lombok.RequiredArgsConstructor;
import utils.Vector3;

import static java.lang.Math.PI;

@RequiredArgsConstructor
public class CameraController {
    private final Camera camera;
    private final FrameCalculator frameCalculator;
    private float speed = 0.1f;

    public void keyReleased(char key) {
        Vector3 camPos = camera.getPosition();
        Vector3 camDir = camera.getDirection();
        Vector3 camUp = camera.getUpVector();
        Vector3 axis;
        float rotationSpeed = 2 * (float) PI / 64f;

        switch (key) {
            //Up
            case 'r':
                camPos.add(camUp.clone()
                        .normalizeV()
                        .scaleV(speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Down
            case 'w':
                camPos.add(camUp.clone()
                        .normalizeV()
                        .scaleV(-1 * speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Forward
            case 'e':
                camPos.add(camDir.clone()
                        .normalizeV()
                        .scaleV(speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Backwards
            case 'd':
                camPos.add(camDir.clone()
                        .normalizeV()
                        .negateV()
                        .scaleV(speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Left
            case 'f':
                camPos.addV(camPos.clone()
                        .crossV(camDir, camUp)
                        .normalizeV()
                        .negateV()
                        .scaleV(speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Right
            case 's':
                camPos.addV(camPos.clone()
                        .crossV(camDir, camUp)
                        .normalizeV()
                        .scaleV(speed));
                frameCalculator.triggerFrameRestart();
                break;

            //Roll Left
            case 'o':
                camUp = camUp.rotateCC(camDir, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            //Roll Right
            case 'u':
                camUp = camUp.rotate(camDir, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            //Pitch Up
            case 'k':
                axis = camDir.clone()
                        .crossV(camDir, camUp)
                        .normalizeV();

                camUp.rotateCC(axis, rotationSpeed);
                camDir.rotateCC(axis, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            //Pitch Down
            case 'i':
                axis = camDir.clone()
                        .crossV(camDir, camUp)
                        .normalizeV();

                camUp.rotate(axis, rotationSpeed);
                camDir.rotate(axis, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            //Yaw Left
            case 'l':
                camDir.rotateCC(camUp, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            //Yaw Right
            case 'j':
                camDir.rotate(camUp, rotationSpeed);
                frameCalculator.triggerFrameRestart();
                break;

            case 't':
                speed *= 2;
                System.out.println("Speed: " + speed);
                break;

            case 'g':
                speed /= 2;
                System.out.print("Speed: " + speed);
                break;
        }
        System.out.printf("\n\n        camera.setPosition(new Vector3(%ff, %ff, %ff));\n" +
                        "        camera.setUpVector(new Vector3(%ff, %ff, %ff));\n" +
                        "        camera.setDirection(new Vector3(%ff, %ff, %ff));\n",
                camPos.getX(), camPos.getY(), camPos.getZ(),
                camUp.getX(), camUp.getY(), camUp.getZ(),
                camDir.getX(), camDir.getY(), camDir.getZ()
        );

        camera.setPosition(camPos);
        camera.setUpVector(camUp);
        camera.setDirection(camDir);
    }
}

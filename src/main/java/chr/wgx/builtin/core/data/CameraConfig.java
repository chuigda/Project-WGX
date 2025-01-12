package chr.wgx.builtin.core.data;

import org.joml.Vector3f;

public final class CameraConfig {
    public float fov;
    public Vector3f cameraPosition;
    public Vector3f lookAtPosition;

    public CameraConfig() {
        fov = 45.0f;
        cameraPosition = new Vector3f(0.0f, 1.0f, 1.0f);
        lookAtPosition = new Vector3f(0.0f, 0.0f, 0.0f);
    }
}

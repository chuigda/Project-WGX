package chr.wgx.builtin.core.data;

import chr.wgx.reactor.Radioactive;
import chr.wgx.reactor.Reactor;
import org.joml.Vector3f;

public final class CameraConfig {
    public final Radioactive<Float> fov;
    public final Radioactive<Vector3f> cameraPosition;
    public final Radioactive<Vector3f> lookAtPosition;

    public CameraConfig(Reactor reactor) {
        fov = new Radioactive<>(45.0f);
        cameraPosition = new Radioactive<>(new Vector3f(0.0f, 1.0f, 1.0f));
        lookAtPosition = new Radioactive<>(new Vector3f(0.0f, 0.0f, 0.0f));
    }
}

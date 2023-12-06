package tech.icey.babel;

import tech.icey.r77.math.Vector3;
import tech.icey.util.Radioactive;

public final class ModelPosition implements Radioactive {
    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getRotation() {
        return rotation;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        this.isDirty = true;
    }

    public void setRotation(Vector3 rotation) {
        this.rotation = rotation;
        this.isDirty = true;
    }

    @Override
    public boolean dirty() {
        return isDirty;
    }

    @Override
    public void clearDirty() {
        this.isDirty = false;
    }

    private Vector3 position = Vector3.ZERO;
    private Vector3 rotation = Vector3.ZERO;
    private boolean isDirty = true;
}

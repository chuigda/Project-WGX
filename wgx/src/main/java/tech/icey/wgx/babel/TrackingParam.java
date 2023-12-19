package tech.icey.wgx.babel;

import tech.icey.r77.math.Vector3;
import tech.icey.util.Radioactive;

public final class TrackingParam implements Radioactive {
    public Vector3 getTranslation() {
        return this.translation;
    }

    public Vector3 getFaceAngle() {
        return this.faceAngle;
    }

    public float getMouthOpen() {
        return this.mouthOpen;
    }

    public float getEyeOpenLeft() {
        return this.eyeOpenLeft;
    }

    public float getEyeOpenRight() {
        return this.eyeOpenRight;
    }

    public void setTranslation(Vector3 translation) {
        this.translation = translation;
        this.isDirty = true;
    }

    public void setFaceAngle(Vector3 faceAngle) {
        this.faceAngle = faceAngle;
        this.isDirty = true;
    }

    public void setMouthOpen(float mouthOpen) {
        this.mouthOpen = mouthOpen;
        this.isDirty = true;
    }

    public void setEyeOpenLeft(float eyeOpenLeft) {
        this.eyeOpenLeft = eyeOpenLeft;
        this.isDirty = true;
    }

    public void setEyeOpenRight(float eyeOpenRight) {
        this.eyeOpenRight = eyeOpenRight;
        this.isDirty = true;
    }

    @Override
    public boolean dirty() {
        return this.isDirty;
    }

    @Override
    public void clearDirty() {
        this.isDirty = false;
    }

    private Vector3 translation = Vector3.ZERO;
    private Vector3 faceAngle = Vector3.ZERO;
    private float mouthOpen = 0;
    private float eyeOpenLeft = 0;
    private float eyeOpenRight = 0;

    private boolean isDirty = true;
}

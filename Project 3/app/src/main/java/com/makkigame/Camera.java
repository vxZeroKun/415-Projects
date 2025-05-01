package com.makkigame;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Manages the camera's position and orientation in 3D space.
 */
public class Camera {
    private Vector3f position;
    private float pitch;
    private float yaw;
    private final float speed = 10.0f;
    private final float sensitivity = 0.1f;

    private boolean firstUpdate = true;
    private double lastMouseX, lastMouseY;

    public Camera(float x, float y, float z, float pitch, float yaw) {
        this.position = new Vector3f(x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /** Adjusts orientation based on mouse movement. */
    public void updateOrientation(double mouseX, double mouseY) {
        if (firstUpdate) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstUpdate = false;
        }
        double dx = (mouseX - lastMouseX) * sensitivity;
        double dy = (lastMouseY - mouseY) * sensitivity;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        yaw = (yaw + (float) dx) % 360;
        pitch = Math.max(-89f, Math.min(89f, pitch + (float) dy));
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /** Returns the normalized forward (look) vector. */
    public Vector3f getDirection() {
        return getForward(); // getForward() is already computing it
    }

    /** Moves camera forward/back along its facing direction. */
    public void moveForward(float delta) {
        Vector3f dir = getForward();
        position.fma(speed * delta, dir);
    }

    /** Strafes camera left/right. */
    public void strafe(float delta) {
        Vector3f dir = getForward();
        Vector3f right = new Vector3f(-dir.z, 0, dir.x).normalize();
        position.fma(speed * delta, right);
    }

    /** Flies camera up/down. Positive = up, negative = down. */
    public void fly(float delta) {
        position.y += speed * delta;
    }

    /** Builds the view matrix for rendering. */
    public Matrix4f getViewMatrix() {
        Vector3f eye = new Vector3f(position);
        Vector3f center = new Vector3f(position).add(getForward());
        return new Matrix4f().lookAt(eye, center, new Vector3f(0, 1, 0));
    }

    /** Computes the forward (look) vector from pitch/yaw. */
    private Vector3f getForward() {
        float cosP = (float) Math.cos(Math.toRadians(pitch));
        float sinP = (float) Math.sin(Math.toRadians(pitch));
        float cosY = (float) Math.cos(Math.toRadians(yaw));
        float sinY = (float) Math.sin(Math.toRadians(yaw));
        return new Vector3f(sinY * cosP, sinP, cosY * cosP).normalize();
    }
}

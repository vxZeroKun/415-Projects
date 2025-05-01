package com.makkigame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;
    public Camera camera;
    private double lastTime;

    private Shaders shader;
    private Texture texture;
    private WorldBase worldBase;
    private Model worldModel;
    private Model cudebModel;

    // for block-break animations
    private static class BreakingBlock {
        int x, y, z;
        float start;

        BreakingBlock(int x, int y, int z, float s) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.start = s;
        }
    }

    private List<BreakingBlock> breaking = new ArrayList<>();
    private float breakAnimDur = 0.5f;

    private float destroyDelay = 0.2f, buildDelay = 0.2f;
    private float destroyTimer = 0, buildTimer = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        init();
        loop();
        // cleanup
        worldModel.destroy();
        cudebModel.destroy();
        shader.destroy();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Request OpenGL 3.3 core
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(1000, 800, "Makki's Project 3 Engine", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create GLFW window");

        // Input callbacks
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
        });
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> camera.updateOrientation(xpos, ypos));
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.05f, 0.05f, 0.2f, 1f);

        // Setup camera and time
        camera = new Camera(8, 20, -20, -30, 0);
        lastTime = glfwGetTime();

        // Load resources
        shader = new Shaders("shaders/vertex.glsl", "shaders/fragment.glsl");
        texture = new Texture("textures/grass.png");
        worldBase = new WorldBase();
        worldModel = ModelGenerator.generetModel(worldBase);
        cudebModel = generateUnitCubeMesh();
    }

    private void loop() {
        Matrix4f projection = new Matrix4f()
                .perspective((float) Math.toRadians(70), 800f / 600f, 0.1f, 100f);

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float dt = (float) (now - lastTime);
            lastTime = now;

            destroyTimer -= dt;
            buildTimer -= dt;
            processInput(dt);

            // Handle break animations
            Iterator<BreakingBlock> iter = breaking.iterator();
            while (iter.hasNext()) {
                BreakingBlock bb = iter.next();
                if ((now - bb.start) >= breakAnimDur) {
                    worldBase.destroyBlock(bb.x, bb.y, bb.z);
                    iter.remove();
                    worldModel.destroy();
                    worldModel = ModelGenerator.generetModel(worldBase);
                }
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f view = camera.getViewMatrix();
            Matrix4f model = new Matrix4f().identity();

            shader.bind();
            shader.setUniform("projection", projection);
            shader.setUniform("view", view);
            shader.setUniform("model", model);
            shader.setUniform("lightPos", new Vector3f(10, 10, 10));
            shader.setUniform("viewPos", camera.getPosition());
            shader.setUniform("lightColor", new Vector3f(1, 1, 1));
            shader.setUniform("objectColor", new Vector3f(1, 1, 1));

            texture.activate(0);
            shader.setUniform("textureSampler", 0);

            // Render world
            worldModel.draw();

            // Render break animation cubes
            for (BreakingBlock bb : breaking) {
                float p = (float) ((now - bb.start) / breakAnimDur);
                float s = 1f - p;
                Matrix4f m = new Matrix4f()
                        .translate(bb.x, bb.y, bb.z)
                        .translate(0.5f, 0.5f, 0.5f)
                        .scale(s)
                        .translate(-0.5f, -0.5f, -0.5f);
                shader.setUniform("model", m);
                cudebModel.draw();
            }

            shader.unbind();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void processInput(float dt) {
        // WASD
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.moveForward(dt);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.moveForward(-dt);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.strafe(-dt);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.strafe(dt);
        }

        // fly up / down
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.fly(dt); // positive = up
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            camera.fly(-dt); // negative = down
        }

        // Block actions
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS && destroyTimer <= 0) {
            raycastDestroy();
            destroyTimer = destroyDelay;
        }
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS && buildTimer <= 0) {
            raycastPlace();
            buildTimer = buildDelay;
        }
    }

    private void raycastDestroy() {
        float maxD = 10f, step = 0.1f;
        Vector3f dir = camera.getDirection();
        Vector3f pos = camera.getPosition();
        double now = glfwGetTime();

        for (float t = 0; t < maxD; t += step) {
            int bx = (int) Math.floor(pos.x + dir.x * t);
            int by = (int) Math.floor(pos.y + dir.y * t);
            int bz = (int) Math.floor(pos.z + dir.z * t);
            if (worldBase.getBlock(bx, by, bz) != MaterialType.EMPTY) {
                breaking.add(new BreakingBlock(bx, by, bz, (float) now));
                break;
            }
        }
    }

    private void raycastPlace() {
        float maxD = 10f, step = 0.1f;
        Vector3f dir = camera.getDirection();
        Vector3f pos = camera.getPosition();
        float lx = pos.x, ly = pos.y, lz = pos.z;
        boolean hit = false;

        for (float t = 0.5f; t < maxD; t += step) {
            float x = pos.x + dir.x * t;
            float y = pos.y + dir.y * t;
            float z = pos.z + dir.z * t;
            int bx = (int) Math.floor(x);
            int by = (int) Math.floor(y);
            int bz = (int) Math.floor(z);
            if (worldBase.getBlock(bx, by, bz) != MaterialType.EMPTY) {
                int px = (int) Math.floor(lx);
                int py = (int) Math.floor(ly);
                int pz = (int) Math.floor(lz);
                if (worldBase.getBlock(px, py, pz) == MaterialType.EMPTY) {
                    worldBase.placeBlock(px, py, pz, MaterialType.GRASS);
                }
                hit = true;
                break;
            }
            lx = x;
            ly = y;
            lz = z;
        }

        if (!hit) {
            int px = (int) Math.floor(lx);
            int py = (int) Math.floor(ly);
            int pz = (int) Math.floor(lz);
            worldBase.placeBlock(px, py, pz, MaterialType.GRASS);
        }

        // rebuild mesh
        worldModel.destroy();
        worldModel = ModelGenerator.generetModel(worldBase);
    }

    private Model generateUnitCubeMesh() {
        float[] V = {
                // front face
                0, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 1, 1, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 0, 0, 1,
                0, 1, 1, 0, 1, 0, 0, 1,
                // back face
                1, 0, 0, 0, 0, 0, 0, -1,
                0, 0, 0, 1, 0, 0, 0, -1,
                0, 1, 0, 1, 1, 0, 0, -1,
                1, 1, 0, 0, 1, 0, 0, -1,
                // left face
                0, 0, 0, 0, 0, -1, 0, 0,
                0, 0, 1, 1, 0, -1, 0, 0,
                0, 1, 1, 1, 1, -1, 0, 0,
                0, 1, 0, 0, 1, -1, 0, 0,
                // right face
                1, 0, 1, 0, 0, 1, 0, 0,
                1, 0, 0, 1, 0, 1, 0, 0,
                1, 1, 0, 1, 1, 1, 0, 0,
                1, 1, 1, 0, 1, 1, 0, 0,
                // top face
                0, 1, 1, 0, 0, 0, 1, 0,
                1, 1, 1, 1, 0, 0, 1, 0,
                1, 1, 0, 1, 1, 0, 1, 0,
                0, 1, 0, 0, 1, 0, 1, 0,
                // bottom face
                1, 0, 1, 0, 0, 0, -1, 0,
                0, 0, 1, 1, 0, 0, -1, 0,
                0, 0, 0, 1, 1, 0, -1, 0,
                1, 0, 0, 0, 1, 0, -1, 0
        };
        int[] I = {
                0, 1, 2, 2, 3, 0,
                4, 5, 6, 6, 7, 4,
                8, 9, 10, 10, 11, 8,
                12, 13, 14, 14, 15, 12,
                16, 17, 18, 18, 19, 16,
                20, 21, 22, 22, 23, 20
        };
        return new Model(V, I);
    }
}

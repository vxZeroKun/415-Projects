package com.makkigame;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.lwjgl.opengl.GL20.*;

public class Shaders {
    private final int programId;

    public Shaders(String vert, String frag) {
        int vShader = compileShader(vert, GL_VERTEX_SHADER);
        int fShader = compileShader(frag, GL_FRAGMENT_SHADER);

        programId = glCreateProgram();
        if (programId == 0)
            throw new RuntimeException("Could not create program");

        glAttachShader(programId, vShader);
        glAttachShader(programId, fShader);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program link error: " + glGetProgramInfoLog(programId, 1024));
        }

        // shaders can be deleted once linked
        glDetachShader(programId, vShader);
        glDetachShader(programId, fShader);
        glDeleteShader(vShader);
        glDeleteShader(fShader);
    }

    private int compileShader(String resource, int type) {
        try {
            String src = Files.readString(Path.of(getClass()
                    .getClassLoader()
                    .getResource(resource)
                    .toURI()));
            int shader = glCreateShader(type);
            glShaderSource(shader, src);
            glCompileShader(shader);
            if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
                throw new RuntimeException(
                        "Shader compile error (" + resource + "): " +
                                glGetShaderInfoLog(shader, 1024));
            }
            return shader;
        } catch (Exception e) {
            throw new RuntimeException("Failed loading shader: " + resource, e);
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void destroy() {
        glDeleteProgram(programId);
    }

    public void setUniform(String name, Matrix4f m) {
        int loc = glGetUniformLocation(programId, name);
        float[] buf = new float[16];
        m.get(buf);
        glUniformMatrix4fv(loc, false, buf);
    }

    public void setUniform(String name, Vector3f v) {
        int loc = glGetUniformLocation(programId, name);
        glUniform3f(loc, v.x, v.y, v.z);
    }

    public void setUniform(String name, int x) {
        glUniform1i(glGetUniformLocation(programId, name), x);
    }
}

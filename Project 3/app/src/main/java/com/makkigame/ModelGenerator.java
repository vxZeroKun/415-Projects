package com.makkigame;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a mesh from a worldBase using MaterialType for UV offsets and tinting.
 */
public class ModelGenerator {

    public static Model generetModel(WorldBase worldBase) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int offset = 0;

        for (int x = 0; x < WorldBase.WIDTH; x++) {
            for (int y = 0; y < WorldBase.HEIGHT; y++) {
                for (int z = 0; z < WorldBase.DEPTH; z++) {
                    MaterialType mat = worldBase.getBlock(x, y, z);
                    if (mat == MaterialType.EMPTY)
                        continue;

                    float uOff = mat.getU();
                    float vOff = mat.getV();

                    // Front face
                    if (worldBase.getBlock(x, y, z + 1) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x, y, z + 1 },
                                new float[] { x + 1, y, z + 1 },
                                new float[] { x + 1, y + 1, z + 1 },
                                new float[] { x, y + 1, z + 1 },
                                0, 0, 1, offset, uOff, vOff);
                        offset += 4;
                    }
                    // Back face
                    if (worldBase.getBlock(x, y, z - 1) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x + 1, y, z },
                                new float[] { x, y, z },
                                new float[] { x, y + 1, z },
                                new float[] { x + 1, y + 1, z },
                                0, 0, -1, offset, uOff, vOff);
                        offset += 4;
                    }
                    // Left face
                    if (worldBase.getBlock(x - 1, y, z) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x, y, z },
                                new float[] { x, y, z + 1 },
                                new float[] { x, y + 1, z + 1 },
                                new float[] { x, y + 1, z },
                                -1, 0, 0, offset, uOff, vOff);
                        offset += 4;
                    }
                    // Right face
                    if (worldBase.getBlock(x + 1, y, z) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x + 1, y, z + 1 },
                                new float[] { x + 1, y, z },
                                new float[] { x + 1, y + 1, z },
                                new float[] { x + 1, y + 1, z + 1 },
                                1, 0, 0, offset, uOff, vOff);
                        offset += 4;
                    }
                    // Top face
                    if (worldBase.getBlock(x, y + 1, z) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x, y + 1, z },
                                new float[] { x + 1, y + 1, z },
                                new float[] { x + 1, y + 1, z + 1 },
                                new float[] { x, y + 1, z + 1 },
                                0, 1, 0, offset, uOff, vOff);
                        offset += 4;
                    }
                    // Bottom face
                    if (worldBase.getBlock(x, y - 1, z) == MaterialType.EMPTY) {
                        addFace(vertices, indices,
                                new float[] { x + 1, y, z },
                                new float[] { x, y, z },
                                new float[] { x, y, z + 1 },
                                new float[] { x + 1, y, z + 1 },
                                0, -1, 0, offset, uOff, vOff);
                        offset += 4;
                    }
                }
            }
        }

        float[] vArray = new float[vertices.size()];
        int[] iArray = new int[indices.size()];
        for (int i = 0; i < vArray.length; i++)
            vArray[i] = vertices.get(i);
        for (int i = 0; i < iArray.length; i++)
            iArray[i] = indices.get(i);

        return new Model(vArray, iArray);
    }

    private static void addFace(
            List<Float> vs, List<Integer> is,
            float[] v0, float[] v1, float[] v2, float[] v3,
            float nx, float ny, float nz,
            int off, float uOff, float vOff) {
        // vertex: position (3), texcoord (2), normal (3)
        addVertex(vs, v0, uOff, vOff, nx, ny, nz);
        addVertex(vs, v1, uOff + MaterialType.UV_TILE_SIZE, vOff, nx, ny, nz);
        addVertex(vs, v2, uOff + MaterialType.UV_TILE_SIZE, vOff + MaterialType.UV_TILE_SIZE, nx, ny, nz);
        addVertex(vs, v3, uOff, vOff + MaterialType.UV_TILE_SIZE, nx, ny, nz);

        is.add(off);
        is.add(off + 1);
        is.add(off + 2);
        is.add(off + 2);
        is.add(off + 3);
        is.add(off);
    }

    private static void addVertex(
            List<Float> vs, float[] pos,
            float u, float v,
            float nx, float ny, float nz) {
        vs.add(pos[0]);
        vs.add(pos[1]);
        vs.add(pos[2]);
        vs.add(u);
        vs.add(v);
        vs.add(nx);
        vs.add(ny);
        vs.add(nz);
    }
}

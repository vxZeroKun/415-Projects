package com.makkigame;

public class WorldBase {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 100;
    public static final int DEPTH = 100;

    private final MaterialType[][][] blocks = new MaterialType[WIDTH][HEIGHT][DEPTH];

    public WorldBase() {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                // compute a height between 0 and, say, 10:
                int hillHeight = (int) (5 + 4 * Math.sin(x * 0.1) * Math.cos(z * 0.1));
                for (int y = 0; y < HEIGHT; y++) {
                    if (y <= hillHeight) {
                        // fill up to hillHeight with dirt, then grass on top
                        blocks[x][y][z] = (y == hillHeight ? MaterialType.GRASS : MaterialType.DIRT);
                    } else {
                        blocks[x][y][z] = MaterialType.EMPTY;
                    }
                }
            }
        }
    }

    public MaterialType getBlock(int x, int y, int z) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH) {
            return MaterialType.EMPTY;
        }
        return blocks[x][y][z];
    }

    public void destroyBlock(int x, int y, int z) {
        if (isValid(x, y, z)) {
            blocks[x][y][z] = MaterialType.EMPTY;
        }
    }

    public void placeBlock(int x, int y, int z, MaterialType type) {
        if (isValid(x, y, z)) {
            blocks[x][y][z] = type;
        }
    }

    private boolean isValid(int x, int y, int z) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && z >= 0 && z < DEPTH;
    }
}
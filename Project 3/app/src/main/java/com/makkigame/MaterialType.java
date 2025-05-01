package com.makkigame;

import org.joml.Vector3f;

/**
 * Defines the types of voxels in the world, their appearance in the texture
 * atlas,
 * and material properties such as hardness and tint color.
 */
public enum MaterialType {
    /** No block (transparent) */
    EMPTY(0, 0, 0f, new Vector3f(0f, 0f, 0f)),
    /** Grass block: top is grass, sides are dirt */
    GRASS(1, 0, 1.2f, new Vector3f(0.1f, 0.8f, 0.1f)),
    /** Dirt block */
    DIRT(2, 0, 0.8f, new Vector3f(0.6f, 0.4f, 0.2f)),
    /** Stone block */
    STONE(3, 0, 2.5f, new Vector3f(0.5f, 0.5f, 0.5f)),
    /** Special block used for marking */
    MARKER(4, 0, 0.5f, new Vector3f(1f, 0f, 1f));

    /** Number of pixels per tile in the atlas */
    public static final float UV_TILE_SIZE = 0.25f; // assuming 4x4 atlas grid

    private final int atlasX;
    private final int atlasY;
    private final float hardness;
    private final Vector3f tint;

    MaterialType(int atlasX, int atlasY, float hardness, Vector3f tint) {
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        this.hardness = hardness;
        this.tint = tint;
    }

    /**
     * @return hardness multiplier used in break animations (higher = slower)
     */
    public float getHardness() {
        return hardness;
    }

    /**
     * @return RGB tint color applied in shader
     */
    public Vector3f getTint() {
        return new Vector3f(tint);
    }

    /**
     * @return U offset (0–1) in the texture atlas
     */
    public float getU() {
        return atlasX * UV_TILE_SIZE;
    }

    /**
     * @return V offset (0–1) in the texture atlas
     */
    public float getV() {
        return atlasY * UV_TILE_SIZE;
    }

    /**
     * Map an ordinal (e.g. saved in chunk data) back to a MaterialType
     * 
     * @param ord saved ordinal (0-based)
     * @return corresponding MaterialType, or EMPTY if out of range
     */
    public static MaterialType fromOrdinal(int ord) {
        if (ord < 0 || ord >= values().length)
            return EMPTY;
        return values()[ord];
    }
}

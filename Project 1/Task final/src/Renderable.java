package src;

import java.awt.Color;

public interface Renderable {
    // Returns an Intersection (or null if no hit) for the given ray.
    public Intersection intersect(Vector O, Vector D, double t_min, double t_max);

    // Material properties:
    public Color getColor();

    public int getSpecular();

    public double getReflective();

    // Returns the surface normal at point P on the object.
    public Vector getNormal(Vector P);

    // Returns the axis-aligned bounding box (AABB) of the object.
    public AABB getBoundingBox();
}

package src;

import java.awt.Color;

public class Sphere implements Renderable {
    public Vector center;
    public double radius;
    public Color color;
    public int specular; // -1 means no specular highlight
    public double reflective; // 0.0 = no reflection, 1.0 = perfect mirror

    public Sphere(Vector center, double radius, Color color, int specular, double reflective) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
    }

    @Override
    public Intersection intersect(Vector O, Vector D, double t_min, double t_max) {
        double[] tVals = RayTracer.intersectRaySphere(O, D, this);
        double t = Double.POSITIVE_INFINITY;
        if (tVals[0] >= t_min && tVals[0] <= t_max && tVals[0] < t) {
            t = tVals[0];
        }
        if (tVals[1] >= t_min && tVals[1] <= t_max && tVals[1] < t) {
            t = tVals[1];
        }
        return (t == Double.POSITIVE_INFINITY) ? null : new Intersection(this, t);
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public int getSpecular() {
        return this.specular;
    }

    @Override
    public double getReflective() {
        return this.reflective;
    }

    @Override
    public Vector getNormal(Vector P) {
        return P.subtract(center).normalize();
    }

    @Override
    public AABB getBoundingBox() {
        Vector min = new Vector(center.x - radius, center.y - radius, center.z - radius);
        Vector max = new Vector(center.x + radius, center.y + radius, center.z + radius);
        return new AABB(min, max);
    }
}

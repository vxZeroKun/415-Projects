package src;

import java.awt.Color;

public class Triangle implements Renderable {
    public Vector v0, v1, v2;
    public Color color;
    public int specular;
    public double reflective;

    public Triangle(Vector v0, Vector v1, Vector v2, Color color, int specular, double reflective) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
    }

    @Override
    public Intersection intersect(Vector O, Vector D, double t_min, double t_max) {
        final double EPSILON = 1e-8;
        Vector edge1 = v1.subtract(v0);
        Vector edge2 = v2.subtract(v0);
        Vector h = D.cross(edge2);
        double a = edge1.dot(h);
        if (Math.abs(a) < EPSILON) {
            return null; // Ray is parallel to the triangle.
        }
        double f = 1.0 / a;
        Vector s = O.subtract(v0);
        double u = f * s.dot(h);
        if (u < 0.0 || u > 1.0) {
            return null;
        }
        Vector q = s.cross(edge1);
        double v = f * D.dot(q);
        if (v < 0.0 || u + v > 1.0) {
            return null;
        }
        double t = f * edge2.dot(q);
        if (t < t_min || t > t_max) {
            return null;
        }
        return new Intersection(this, t);
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
        Vector edge1 = v1.subtract(v0);
        Vector edge2 = v2.subtract(v0);
        return edge1.cross(edge2).normalize();
    }

    @Override
    public AABB getBoundingBox() {
        double minX = Math.min(v0.x, Math.min(v1.x, v2.x));
        double minY = Math.min(v0.y, Math.min(v1.y, v2.y));
        double minZ = Math.min(v0.z, Math.min(v1.z, v2.z));
        double maxX = Math.max(v0.x, Math.max(v1.x, v2.x));
        double maxY = Math.max(v0.y, Math.max(v1.y, v2.y));
        double maxZ = Math.max(v0.z, Math.max(v1.z, v2.z));
        return new AABB(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }
}

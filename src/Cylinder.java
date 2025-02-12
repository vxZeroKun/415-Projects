package src;

import java.awt.Color;

public class Cylinder implements Renderable {
    // The cylinder is now aligned along the y-axis.
    // Its center is (Cx, Cy, Cz) and it extends from y = Cy - h/2 to y = Cy + h/2.
    // The side surface is defined by (x - Cx)^2 + (z - Cz)^2 = r^2.
    public Vector center;
    public double radius;
    public double height;

    public Color color;
    public int specular;
    public double reflective;

    public Cylinder(Vector center, double radius, double height, Color color, int specular, double reflective) {
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
    }

    @Override
    public Intersection intersect(Vector O, Vector D, double t_min, double t_max) {
        double tSide = Double.POSITIVE_INFINITY;
        double tCapCandidate = Double.POSITIVE_INFINITY;
        double tClosest = Double.POSITIVE_INFINITY;

        // --- 1. Solve for Side Intersections ---
        // Use the x and z components:
        // A = Dx^2 + Dz^2,
        double A = D.x * D.x + D.z * D.z;
        // B = 2 * ((Ox - Cx)*Dx + (Oz - Cz)*Dz)
        double B = 2 * ((O.x - center.x) * D.x + (O.z - center.z) * D.z);
        // C_val = (Ox - Cx)^2 + (Oz - Cz)^2 - r^2
        double C_val = (O.x - center.x) * (O.x - center.x) + (O.z - center.z) * (O.z - center.z) - radius * radius;
        double disc = B * B - 4 * A * C_val;
        boolean hitSide = false;
        if (disc >= 0 && A != 0) {
            double sqrtDisc = Math.sqrt(disc);
            double t1 = (-B - sqrtDisc) / (2 * A);
            double t2 = (-B + sqrtDisc) / (2 * A);

            double yMin = center.y - height / 2;
            double yMax = center.y + height / 2;

            if (t1 > 0) {
                double y1 = O.y + t1 * D.y;
                if (y1 >= yMin && y1 <= yMax) {
                    tSide = t1;
                    hitSide = true;
                }
            }
            if (t2 > 0) {
                double y2 = O.y + t2 * D.y;
                if (y2 >= yMin && y2 <= yMax) {
                    if (!hitSide || t2 < tSide) {
                        tSide = t2;
                        hitSide = true;
                    }
                }
            }
        }

        // --- 2. Solve for Cap Intersections ---
        // The caps are located at y = center.y ± height/2.
        double yTop = center.y + height / 2;
        double yBottom = center.y - height / 2;
        if (D.y != 0) {
            double tTop = (yTop - O.y) / D.y;
            if (tTop > 0) {
                Vector P_top = O.add(D.scale(tTop));
                double dx = P_top.x - center.x;
                double dz = P_top.z - center.z;
                if (dx * dx + dz * dz <= radius * radius) {
                    tCapCandidate = tTop;
                }
            }
            double tBottom = (yBottom - O.y) / D.y;
            if (tBottom > 0) {
                Vector P_bottom = O.add(D.scale(tBottom));
                double dx = P_bottom.x - center.x;
                double dz = P_bottom.z - center.z;
                if (dx * dx + dz * dz <= radius * radius) {
                    if (tBottom < tCapCandidate) {
                        tCapCandidate = tBottom;
                    }
                }
            }
        }

        tClosest = Math.min(tSide, tCapCandidate);
        if (tClosest < t_min || tClosest > t_max || tClosest == Double.POSITIVE_INFINITY) {
            return null;
        }

        return new Intersection(this, tClosest);
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

    // Compute the normal at point P.
    // For a side intersection, the normal is (P.x - Cx, 0, P.z - Cz) normalized.
    // For a cap intersection, the normal is (0, ±1, 0) (depending on whether it is
    // the top or bottom cap).
    @Override
    public Vector getNormal(Vector P) {
        double epsilon = 1e-4;
        double yTop = center.y + height / 2;
        double yBottom = center.y - height / 2;
        if (Math.abs(P.y - yTop) < epsilon) {
            return new Vector(0, 1, 0);
        } else if (Math.abs(P.y - yBottom) < epsilon) {
            return new Vector(0, -1, 0);
        } else {
            return new Vector(P.x - center.x, 0, P.z - center.z).normalize();
        }
    }

    @Override
    public AABB getBoundingBox() {
        Vector min = new Vector(center.x - radius, center.y - height / 2, center.z - radius);
        Vector max = new Vector(center.x + radius, center.y + height / 2, center.z + radius);
        return new AABB(min, max);
    }
}

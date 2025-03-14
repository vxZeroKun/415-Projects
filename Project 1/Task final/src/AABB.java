package src;

public class AABB {
    public Vector min;
    public Vector max;

    public AABB(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    // Returns a bounding box that surrounds two boxes.
    public static AABB surroundingBox(AABB box0, AABB box1) {
        Vector small = new Vector(
                Math.min(box0.min.x, box1.min.x),
                Math.min(box0.min.y, box1.min.y),
                Math.min(box0.min.z, box1.min.z));
        Vector big = new Vector(
                Math.max(box0.max.x, box1.max.x),
                Math.max(box0.max.y, box1.max.y),
                Math.max(box0.max.z, box1.max.z));
        return new AABB(small, big);
    }

    // Ray-AABB intersection test using the slab method.
    public boolean intersect(Vector O, Vector D, double t_min, double t_max) {
        for (int a = 0; a < 3; a++) {
            double origin = (a == 0) ? O.x : (a == 1) ? O.y : O.z;
            double direction = (a == 0) ? D.x : (a == 1) ? D.y : D.z;
            double minVal = (a == 0) ? min.x : (a == 1) ? min.y : min.z;
            double maxVal = (a == 0) ? max.x : (a == 1) ? max.y : max.z;
            double invD = 1.0 / direction;
            double t0 = (minVal - origin) * invD;
            double t1 = (maxVal - origin) * invD;
            if (invD < 0.0) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }
            t_min = (t0 > t_min) ? t0 : t_min;
            t_max = (t1 < t_max) ? t1 : t_max;
            if (t_max <= t_min)
                return false;
        }
        return true;
    }
}

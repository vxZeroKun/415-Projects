package src;

import java.awt.Color;
import java.util.*;

public class BVHNode implements Renderable {
    public AABB box;
    public BVHNode left;
    public BVHNode right;
    public Renderable object; // Non-null only for leaf nodes.

    // Recursively builds a BVH from objects[start:end].
    public BVHNode(List<Renderable> objects, int start, int end) {
        int objectSpan = end - start;
        if (objectSpan == 1) {
            object = objects.get(start);
            left = right = null;
            box = object.getBoundingBox();
        } else if (objectSpan == 2) {
            Renderable obj0 = objects.get(start);
            Renderable obj1 = objects.get(start + 1);
            left = new BVHNode(objects, start, start + 1);
            right = new BVHNode(objects, start + 1, start + 2);
            box = AABB.surroundingBox(left.box, right.box);
        } else {
            // Choose a random axis (0=x, 1=y, 2=z) to sort along.
            int axis = (int) (Math.random() * 3);
            Comparator<Renderable> comparator = (a, b) -> {
                double aCenter, bCenter;
                AABB boxA = a.getBoundingBox();
                AABB boxB = b.getBoundingBox();
                if (axis == 0) {
                    aCenter = (boxA.min.x + boxA.max.x) * 0.5;
                    bCenter = (boxB.min.x + boxB.max.x) * 0.5;
                } else if (axis == 1) {
                    aCenter = (boxA.min.y + boxA.max.y) * 0.5;
                    bCenter = (boxB.min.y + boxB.max.y) * 0.5;
                } else {
                    aCenter = (boxA.min.z + boxA.max.z) * 0.5;
                    bCenter = (boxB.min.z + boxB.max.z) * 0.5;
                }
                return Double.compare(aCenter, bCenter);
            };
            objects.subList(start, end).sort(comparator);
            int mid = start + objectSpan / 2;
            left = new BVHNode(objects, start, mid);
            right = new BVHNode(objects, mid, end);
            box = AABB.surroundingBox(left.box, right.box);
            object = null;
        }
    }

    @Override
    public Intersection intersect(Vector O, Vector D, double t_min, double t_max) {
        if (!box.intersect(O, D, t_min, t_max)) {
            return null;
        }

        if (object != null) {
            // Leaf node.
            return object.intersect(O, D, t_min, t_max);
        }

        Intersection leftHit = left != null ? left.intersect(O, D, t_min, t_max) : null;
        Intersection rightHit = right != null ? right.intersect(O, D, t_min, t_max) : null;

        if (leftHit != null && rightHit != null) {
            return (leftHit.t < rightHit.t) ? leftHit : rightHit;
        } else if (leftHit != null) {
            return leftHit;
        } else {
            return rightHit;
        }
    }

    // The following methods are implemented only to satisfy the Renderable
    // interface.
    @Override
    public Color getColor() {
        return (object != null) ? object.getColor() : null;
    }

    @Override
    public int getSpecular() {
        return (object != null) ? object.getSpecular() : -1;
    }

    @Override
    public double getReflective() {
        return (object != null) ? object.getReflective() : 0;
    }

    @Override
    public Vector getNormal(Vector P) {
        return (object != null) ? object.getNormal(P) : new Vector(0, 0, 0);
    }

    @Override
    public AABB getBoundingBox() {
        return box;
    }
}

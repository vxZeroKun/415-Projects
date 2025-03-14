package src;

import java.awt.Color;

public class RayTracer {
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    public static Vector canvasToViewport(int x, int y, int canvasWidth, int canvasHeight, double viewportWidth,
            double viewportHeight, double projectionPlaneD) {
        return new Vector(
                x * viewportWidth / canvasWidth,
                y * viewportHeight / canvasHeight,
                projectionPlaneD);
    }

    public static Color traceRay(Vector O, Vector D, double t_min, double t_max, BVHNode bvh, Light[] lights,
            int recursionDepth) {
        if (recursionDepth <= 0) {
            return Color.WHITE; // Base case for recursion
        }

        Intersection intersection = bvh.intersect(O, D, t_min, t_max);
        if (intersection == null) {
            // Create a gradient background:
            // When the ray is pointing downward (unitDirection.y ~ -1) we use white,
            // and when upward (unitDirection.y ~ 1) we blend to a light blue.
            Vector unitDirection = D.normalize();
            double t = 0.5 * (unitDirection.y + 1.0);

            // White color (for the bottom of the sky) and light blue (for the top)
            // You can tweak these values for a different look.
            Color white = new Color(255, 255, 255);
            Color lightBlue = new Color(128, 179, 255); // (0.5, 0.7, 1.0) in 0-255 scale

            int r = (int) ((1.0 - t) * white.getRed() + t * lightBlue.getRed());
            int g = (int) ((1.0 - t) * white.getGreen() + t * lightBlue.getGreen());
            int b = (int) ((1.0 - t) * white.getBlue() + t * lightBlue.getBlue());

            return new Color(r, g, b);
        }

        Renderable object = intersection.object;
        double closestT = intersection.t;

        // Compute local color.
        Vector P = O.add(D.scale(closestT)); // Intersection point.
        Vector N = object.getNormal(P); // Normal at the intersection.
        Vector V = D.scale(-1); // View direction.

        double intensity = computeLighting(P, N, V, object.getSpecular(), lights, bvh);
        int r = Math.max(0, Math.min(255, (int) (object.getColor().getRed() * intensity)));
        int g = Math.max(0, Math.min(255, (int) (object.getColor().getGreen() * intensity)));
        int b = Math.max(0, Math.min(255, (int) (object.getColor().getBlue() * intensity)));
        Color localColor = new Color(r, g, b);

        // Reflections.
        double reflective = object.getReflective();
        if (reflective <= 0) {
            return localColor;
        }

        Vector R = reflectRay(V, N); // Reflected direction.
        Color reflectedColor = traceRay(P, R, 0.001, Double.POSITIVE_INFINITY, bvh, lights, recursionDepth - 1);

        // Combine local and reflected colors.
        int rFinal = Math.max(0,
                Math.min(255, (int) (localColor.getRed() * (1 - reflective) + reflectedColor.getRed() * reflective)));
        int gFinal = Math.max(0, Math.min(255,
                (int) (localColor.getGreen() * (1 - reflective) + reflectedColor.getGreen() * reflective)));
        int bFinal = Math.max(0,
                Math.min(255, (int) (localColor.getBlue() * (1 - reflective) + reflectedColor.getBlue() * reflective)));

        return new Color(rFinal, gFinal, bFinal);
    }

    // Computes intersection distances with a sphere (used by Sphere.intersect).
    public static double[] intersectRaySphere(Vector O, Vector D, Sphere sphere) {
        Vector CO = O.subtract(sphere.center);

        double a = D.dot(D);
        double b = 2 * CO.dot(D);
        double c = CO.dot(CO) - sphere.radius * sphere.radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
        }

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);
        return new double[] { t1, t2 };
    }

    // Reflects a ray R with respect to normal N.
    public static Vector reflectRay(Vector R, Vector N) {
        return N.scale(2 * N.dot(R)).subtract(R);
    }

    // Computes the lighting at point P with normal N and view direction V.
    public static double computeLighting(Vector P, Vector N, Vector V, int specular, Light[] lights, BVHNode bvh) {
        double intensity = 0.0;

        for (Light light : lights) {
            Vector L;
            double tMax;
            if (light.type == Light.LightType.AMBIENT) {
                intensity += light.intensity;
                continue;
            } else if (light.type == Light.LightType.POINT) {
                L = light.position.subtract(P);
                tMax = 1.0;
            } else { // Directional light.
                L = light.direction;
                tMax = Double.POSITIVE_INFINITY;
            }

            // Shadow check using the BVH.
            Intersection shadowIntersect = bvh.intersect(P, L, 0.001, tMax);
            if (shadowIntersect != null) {
                continue;
            }

            // Diffuse lighting.
            double nDotL = N.dot(L);
            if (nDotL > 0) {
                intensity += light.intensity * nDotL / (N.length() * L.length());
            }

            // Specular lighting.
            if (specular != -1) {
                Vector R = N.scale(2 * N.dot(L)).subtract(L);
                double rDotV = R.dot(V);
                if (rDotV > 0) {
                    intensity += light.intensity * Math.pow(rDotV / (R.length() * V.length()), specular);
                }
            }
        }

        return intensity;
    }
}

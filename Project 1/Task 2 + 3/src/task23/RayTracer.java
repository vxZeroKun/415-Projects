package task23;

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

    public static Color traceRay(Vector O, Vector D, double t_min, double t_max, Sphere[] spheres, Light[] lights,
            int recursionDepth) {
        if (recursionDepth <= 0) {
            return Color.WHITE; // Base case for recursion
        }

        Intersection intersection = closestIntersection(O, D, t_min, t_max, spheres);
        if (intersection == null) {
            return Color.BLACK; // Background color
        }

        Sphere closestSphere = intersection.sphere;
        double closestT = intersection.t;

        // Compute local color
        Vector P = O.add(D.scale(closestT)); // Intersection point
        Vector N = P.subtract(closestSphere.center).normalize(); // Normal at the intersection
        Vector V = D.scale(-1); // View direction

        double intensity = computeLighting(P, N, V, closestSphere.specular, lights, spheres);
        int r = Math.max(0, Math.min(255, (int) (closestSphere.color.getRed() * intensity)));
        int g = Math.max(0, Math.min(255, (int) (closestSphere.color.getGreen() * intensity)));
        int b = Math.max(0, Math.min(255, (int) (closestSphere.color.getBlue() * intensity)));
        Color localColor = new Color(r, g, b);

        // Reflections
        double reflective = closestSphere.reflective;
        if (reflective <= 0) {
            return localColor;
        }

        Vector R = reflectRay(V, N); // Reflected direction
        Color reflectedColor = traceRay(P, R, 0.001, Double.POSITIVE_INFINITY, spheres, lights, recursionDepth - 1);

        // Combine local and reflected color
        int rFinal = Math.max(0,
                Math.min(255, (int) (localColor.getRed() * (1 - reflective) + reflectedColor.getRed() * reflective)));
        int gFinal = Math.max(0, Math.min(255,
                (int) (localColor.getGreen() * (1 - reflective) + reflectedColor.getGreen() * reflective)));
        int bFinal = Math.max(0,
                Math.min(255, (int) (localColor.getBlue() * (1 - reflective) + reflectedColor.getBlue() * reflective)));

        return new Color(rFinal, gFinal, bFinal);
    }

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

    public static Intersection closestIntersection(Vector O, Vector D, double t_min, double t_max, Sphere[] spheres) {
        double closestT = Double.POSITIVE_INFINITY;
        Sphere closestSphere = null;

        for (Sphere sphere : spheres) {
            double[] t = intersectRaySphere(O, D, sphere);
            if (t[0] >= t_min && t[0] <= t_max && t[0] < closestT) {
                closestT = t[0];
                closestSphere = sphere;
            }
            if (t[1] >= t_min && t[1] <= t_max && t[1] < closestT) {
                closestT = t[1];
                closestSphere = sphere;
            }
        }

        return (closestSphere == null) ? null : new Intersection(closestSphere, closestT);
    }

    public static Vector reflectRay(Vector R, Vector N) {
        return N.scale(2 * N.dot(R)).subtract(R);
    }

    public static double computeLighting(Vector P, Vector N, Vector V, int specular, Light[] lights, Sphere[] spheres) {
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
            } else { // Directional light
                L = light.direction;
                tMax = Double.POSITIVE_INFINITY;
            }

            // Shadow check
            Intersection shadowIntersect = closestIntersection(P, L, 0.001, tMax, spheres);
            if (shadowIntersect != null) {
                continue;
            }

            // Diffuse
            double nDotL = N.dot(L);
            if (nDotL > 0) {
                intensity += light.intensity * nDotL / (N.length() * L.length());
            }

            // Specular
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

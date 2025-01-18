import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class RayTracing {
    static final int CANVAS_WIDTH = 1920;
    static final int CANVAS_HEIGHT = 1080;
    static final double VIEWPORT_WIDTH = 1.0;
    static final double VIEWPORT_HEIGHT = 1.0;
    static final double PROJECTION_PLANE_D = 1.0;
    static final Color BACKGROUND_COLOR = Color.WHITE;

    static class Sphere {
        Vector center;
        double radius;
        Color color;

        public Sphere(Vector center, double radius, Color color) {
            this.center = center;
            this.radius = radius;
            this.color = color;
        }
    }

    static class Vector {
        double x, y, z;

        public Vector(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Vector subtract(Vector other) {
            return new Vector(x - other.x, y - other.y, z - other.z);
        }

        Vector scale(double scalar) {
            return new Vector(x * scalar, y * scalar, z * scalar);
        }

        double dot(Vector other) {
            return x * other.x + y * other.y + z * other.z;
        }

        double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }
    }

    static Vector canvasToViewport(int x, int y) {
        return new Vector(
            x * VIEWPORT_WIDTH / CANVAS_WIDTH,
            y * VIEWPORT_HEIGHT / CANVAS_HEIGHT,
            PROJECTION_PLANE_D
        );
    }

    static Color traceRay(Vector O, Vector D, double t_min, double t_max, Sphere[] spheres) {
        double closest_t = Double.POSITIVE_INFINITY;
        Sphere closest_sphere = null;

        for (Sphere sphere : spheres) {
            double[] t = intersectRaySphere(O, D, sphere);
            if (t[0] >= t_min && t[0] <= t_max && t[0] < closest_t) {
                closest_t = t[0];
                closest_sphere = sphere;
            }
            if (t[1] >= t_min && t[1] <= t_max && t[1] < closest_t) {
                closest_t = t[1];
                closest_sphere = sphere;
            }
        }

        if (closest_sphere == null) {
            return BACKGROUND_COLOR;
        }

        return closest_sphere.color;
    }

    static double[] intersectRaySphere(Vector O, Vector D, Sphere sphere) {
        Vector CO = O.subtract(sphere.center);

        double a = D.dot(D);
        double b = 2 * CO.dot(D);
        double c = CO.dot(CO) - sphere.radius * sphere.radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        }

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);
        return new double[]{t1, t2};
    }

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);

        Sphere[] spheres = {
            new Sphere(new Vector(0, -1, 3), 1, Color.RED),
            new Sphere(new Vector(2, 0, 4), 1, Color.BLUE),
            new Sphere(new Vector(-2, 0, 4), 1, Color.GREEN),
        };

        Vector O = new Vector(0, 0, 0); // Camera position at the origin

        for (int x = -CANVAS_WIDTH / 2; x < CANVAS_WIDTH / 2; x++) {
            for (int y = -CANVAS_HEIGHT / 2; y < CANVAS_HEIGHT / 2; y++) {
                Vector D = canvasToViewport(x, y);
                Color color = traceRay(O, D, 1, Double.POSITIVE_INFINITY, spheres);
                image.setRGB(x + CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - y - 1, color.getRGB());
            }
        }

        try {
            File output = new File("output.png");
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to output.png");
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }
}

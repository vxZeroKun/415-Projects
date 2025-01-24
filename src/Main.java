package src;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {
    private static final int CANVAS_WIDTH = 500;
    private static final int CANVAS_HEIGHT = 500;
    private static final double VIEWPORT_WIDTH = 1.0;
    private static final double VIEWPORT_HEIGHT = 1.0;
    private static final double PROJECTION_PLANE_D = 1.0;
    private static final int MAX_RECURSION_DEPTH = 3; // Control reflection depth

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Define spheres based on the scene setup
        Sphere[] spheres = {
                new Sphere(new Vector(0, -1, 3), 1.0, new Color(255, 0, 0), 500, 0.2), // Red sphere (a bit reflective)
                new Sphere(new Vector(2, 0, 4), 1.0, new Color(0, 0, 255), 500, 0.3), // Blue sphere (more reflective)
                new Sphere(new Vector(-2, 0, 4), 1.0, new Color(0, 255, 0), 10, 0.4), // Green sphere (even more
                                                                                      // reflective)
                new Sphere(new Vector(0, -5001, 0), 5000, new Color(255, 255, 0), 1000, 0.5) // Yellow floor (half
                                                                                             // reflective)
        };

        // Define light sources
        Light[] lights = {
                new Light(Light.LightType.AMBIENT, 0.2, null, null),
                new Light(Light.LightType.POINT, 0.6, new Vector(2, 1, 0), null),
                new Light(Light.LightType.DIRECTIONAL, 0.2, null, new Vector(1, 4, 4))
        };

        Vector O = new Vector(0, 0, 0); // Camera at origin

        // Render the scene
        for (int x = -CANVAS_WIDTH / 2; x < CANVAS_WIDTH / 2; x++) {
            for (int y = -CANVAS_HEIGHT / 2; y < CANVAS_HEIGHT / 2; y++) {
                Vector D = RayTracer.canvasToViewport(x, y, CANVAS_WIDTH, CANVAS_HEIGHT, VIEWPORT_WIDTH,
                        VIEWPORT_HEIGHT, PROJECTION_PLANE_D);
                Color color = RayTracer.traceRay(O, D, 1, Double.POSITIVE_INFINITY, spheres, lights,
                        MAX_RECURSION_DEPTH);
                image.setRGB(x + CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - y - 1, color.getRGB());
            }
        }

        // Save image output
        try {
            File output = new File("output.png");
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to output.png");
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }
}

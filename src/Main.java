package src;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

public class Main {
    // You can adjust these image dimensions. Here we use an aspect ratio of 16:9.
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = (int) (IMAGE_WIDTH / (16.0 / 9.0));
    private static final int MAX_RECURSION_DEPTH = 3; // Reflection depth

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // ====================================================
        // 1. Build the scene (objects) as in "Ray Tracing in One Weekend"
        // ====================================================
        List<Renderable> objectsList = new ArrayList<>();

        // --- Ground Sphere ---
        // (A very large sphere at (0,-1000,0) with radius 1000)
        objectsList.add(new Sphere(
                new Vector(0, -1000, 0),
                1000,
                new Color(128, 128, 128), // mid-gray ground
                -1, // no specular (pure diffuse)
                0.0)); // non-reflective

        // --- Many small spheres (radius = 0.2) placed randomly in a grid ---
        for (int a = -11; a < 11; a++) {
            for (int b = -11; b < 11; b++) {
                double chooseMat = Math.random();
                // Center of the small sphere is randomly offset in x and z, at y = 0.2.
                Vector center = new Vector(
                        a + 0.9 * Math.random(),
                        0.2,
                        b + 0.9 * Math.random());
                // Avoid overcrowding near one of the three large spheres (the metal one at
                // (4,0.2,0)).
                if (center.subtract(new Vector(4, 0.2, 0)).length() > 0.9) {
                    if (chooseMat < 0.8) {
                        // Diffuse material ("lambertian"): random color (simulate by no specular and no
                        // reflection)
                        double r = Math.random() * Math.random();
                        double g = Math.random() * Math.random();
                        double bColor = Math.random() * Math.random();
                        Color diffuse = new Color(
                                (int) (r * 255),
                                (int) (g * 255),
                                (int) (bColor * 255));
                        objectsList.add(new Sphere(center, 0.2, diffuse, -1, 0.0));
                    } else if (chooseMat < 0.95) {
                        // Metal material: brighter, with specular highlight and some reflectivity.
                        double r = 0.5 + 0.5 * Math.random();
                        double g = 0.5 + 0.5 * Math.random();
                        double bColor = 0.5 + 0.5 * Math.random();
                        Color metal = new Color(
                                (int) (r * 255),
                                (int) (g * 255),
                                (int) (bColor * 255));
                        objectsList.add(new Sphere(center, 0.2, metal, 500, 0.5));
                    } else {
                        // Glass material: simulate with a nearly mirror–like sphere.
                        objectsList.add(new Sphere(center, 0.2, Color.WHITE, 500, 0.9));
                    }
                }
            }
        }

        // --- Three Large Feature Spheres ---
        // 1. Glass sphere at (0,1,0)
        objectsList.add(new Sphere(new Vector(0, 1, 0), 1.0, Color.WHITE, 500, 0.9));
        // 2. Diffuse (brownish) sphere at (-4,1,0)
        Color brownish = new Color((int) (0.4 * 255), (int) (0.2 * 255), (int) (0.1 * 255));
        objectsList.add(new Sphere(new Vector(-4, 1, 0), 1.0, brownish, -1, 0.0));
        // 3. Metal sphere at (4,1,0)
        Color metalColor = new Color((int) (0.7 * 255), (int) (0.6 * 255), (int) (0.5 * 255));
        objectsList.add(new Sphere(new Vector(4, 1, 0), 1.0, metalColor, 500, 0.5));

        // Build the BVH (bounding volume hierarchy) to accelerate ray–object
        // intersections.
        BVHNode bvh = new BVHNode(objectsList, 0, objectsList.size());

        // ====================================================
        // 2. Set Up Lighting
        // (Our simple lighting model uses an ambient light plus a couple of other
        // sources.)
        // ====================================================
        Light[] lights = {
                // Increase the ambient light to brighten the scene.
                new Light(Light.LightType.AMBIENT, 0.5, null, null),
                // A bright point light (for example, overhead)
                new Light(Light.LightType.POINT, 0.5, new Vector(10, 10, 10), null),
                // A directional light coming from above/behind the camera
                new Light(Light.LightType.DIRECTIONAL, 0.2, null, new Vector(-1, -1, -1))
        };

        // ====================================================
        // 3. Set Up a Camera (similar to the book)
        // ====================================================
        // In the book, the camera is at lookfrom = (13,2,3) and looks at (0,0,0).
        Vector lookfrom = new Vector(13, 2, 3);
        Vector lookat = new Vector(0, 0, 0);
        Vector vup = new Vector(0, 1, 0);
        double vfov = 20.0; // vertical field-of-view in degrees
        double aspectRatio = (double) IMAGE_WIDTH / IMAGE_HEIGHT;

        double theta = Math.toRadians(vfov);
        double h = Math.tan(theta / 2);
        double viewportHeight = 2.0 * h;
        double viewportWidth = aspectRatio * viewportHeight;
        // In the book, focus_dist is set to 10.0
        double focusDist = 10.0;

        // Compute the orthonormal basis vectors for the camera.
        Vector w = lookfrom.subtract(lookat).normalize();
        Vector u = vup.cross(w).normalize();
        Vector v = w.cross(u);

        // Compute the horizontal and vertical spans of the viewport.
        Vector horizontal = u.scale(viewportWidth * focusDist);
        Vector vertical = v.scale(viewportHeight * focusDist);
        // Compute the lower left corner of the viewport.
        Vector lowerLeftCorner = lookfrom
                .subtract(horizontal.scale(0.5))
                .subtract(vertical.scale(0.5))
                .subtract(w.scale(focusDist));

        // ====================================================
        // 4. Render the Scene
        // ====================================================
        // For each pixel, we compute (s,t) coordinates on the viewport, form a ray, and
        // trace it.
        // Note: Since Java’s image y-axis starts at the top, we flip the y coordinate.
        IntStream.range(0, IMAGE_WIDTH).parallel().forEach(x -> {
            for (int y = 0; y < IMAGE_HEIGHT; y++) {
                double s = (double) x / (IMAGE_WIDTH - 1);
                double t = (double) (IMAGE_HEIGHT - 1 - y) / (IMAGE_HEIGHT - 1);
                // The direction from the camera origin (lookfrom) through the viewport pixel.
                Vector rayDirection = lowerLeftCorner
                        .add(horizontal.scale(s))
                        .add(vertical.scale(t))
                        .subtract(lookfrom)
                        .normalize();
                Color pixelColor = RayTracer.traceRay(lookfrom, rayDirection, 0.001, Double.POSITIVE_INFINITY, bvh,
                        lights, MAX_RECURSION_DEPTH);
                synchronized (image) {
                    image.setRGB(x, y, pixelColor.getRGB());
                }
            }
        });

        // ====================================================
        // 5. Save the Rendered Image
        // ====================================================
        try {
            File output = new File("output.png");
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to output.png");
        } catch (Exception e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }
}

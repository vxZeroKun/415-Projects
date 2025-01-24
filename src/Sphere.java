package src;

import java.awt.Color;

public class Sphere {
    public Vector center;
    public double radius;
    public Color color;
    public int specular; // -1 means no specular effect
    public double reflective; // Reflectivity (0.0 = no reflection, 1.0 = perfect mirror)

    public Sphere(Vector center, double radius, Color color, int specular, double reflective) {
        this.center = center;
        this.radius = radius;
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
    }
}

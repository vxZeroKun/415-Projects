package src;

public class Light {
    public enum LightType {
        AMBIENT, POINT, DIRECTIONAL
    }

    public LightType type;
    public double intensity;
    public Vector position; // For point lights
    public Vector direction; // For directional lights

    public Light(LightType type, double intensity, Vector position, Vector direction) {
        this.type = type;
        this.intensity = intensity;
        this.position = position;
        this.direction = direction;
    }
}

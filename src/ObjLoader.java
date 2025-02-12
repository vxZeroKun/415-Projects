package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.awt.Color;

public class ObjLoader {
    public static ArrayList<Triangle> loadObj(String filename, Color color, int specular, double reflective) {
        ArrayList<Vector> vertices = new ArrayList<>();
        ArrayList<Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    vertices.add(new Vector(x, y, z));
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    // OBJ indices are 1-based; we ignore texture/normal indices if present.
                    int i0 = Integer.parseInt(parts[1].split("/")[0]) - 1;
                    int i1 = Integer.parseInt(parts[2].split("/")[0]) - 1;
                    int i2 = Integer.parseInt(parts[3].split("/")[0]) - 1;
                    Triangle tri = new Triangle(vertices.get(i0), vertices.get(i1), vertices.get(i2), color, specular,
                            reflective);
                    triangles.add(tri);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading OBJ file: " + e.getMessage());
        }

        return triangles;
    }
}

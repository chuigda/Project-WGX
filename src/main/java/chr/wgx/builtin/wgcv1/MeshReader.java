package chr.wgx.builtin.wgcv1;

import org.joml.Vector3f;
import tech.icey.xjbutil.container.Pair;

import java.util.ArrayList;
import java.util.List;

public final class MeshReader {
    public static Pair<float[], int[]> parseV1Mesh(String meshText) {
        String[] lines = meshText.split("\n");
        List<Vector3f> vertexBank = new ArrayList<>();

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(" ");
            String command = parts[0];

            switch (command) {
                case "v" -> {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertexBank.add(new Vector3f(x, y, z));
                }
                case "f" -> {
                    int index1 = Integer.parseInt(parts[1]) - 1;
                    int index2 = Integer.parseInt(parts[2]) - 1;
                    int index3 = Integer.parseInt(parts[3]) - 1;

                    Vector3f v1 = vertexBank.get(index1);
                    Vector3f v2 = vertexBank.get(index2);
                    Vector3f v3 = vertexBank.get(index3);

                    Vector3f v2v1 = new Vector3f(v2).sub(v1);
                    Vector3f v3v2 = new Vector3f(v3).sub(v2);
                    Vector3f normal = v2v1.cross(v3v2).normalize();

                    addVector(vertices, v1); addVector(vertices, normal);
                    addVector(vertices, v2); addVector(vertices, normal);
                    addVector(vertices, v3); addVector(vertices, normal);

                    for (int i = 0; i < 3; i++) {
                        indices.add(indices.size());
                    }
                }
            }

        }

        float[] verticesArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            verticesArray[i] = vertices.get(i);
        }
        int[] indicesArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }

        return new Pair<>(verticesArray, indicesArray);
    }

    private static void addVector(List<Float> output, Vector3f vector) {
        output.add(vector.x);
        output.add(vector.y);
        output.add(vector.z);
    }
}

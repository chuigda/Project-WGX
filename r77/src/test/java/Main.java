import tech.icey.r77.meta.Attribute;
import tech.icey.r77.meta.Vertex;
import tech.icey.r77.math.Vector2;

public class Main {
    @Vertex
    class MyVertex {
        @Attribute(position = 0, name = "coordinate") Vector2<Float> position;
        @Attribute(position = 1, name = "color") Vector2<Float> color;
    }

    public static void main(String[] args) {
        System.out.println(MyVertex.class);
    }
}

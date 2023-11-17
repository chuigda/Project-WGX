import tech.icey.r77.meta.Vertex;
import tech.icey.r77.meta.Attribute;
import tech.icey.r77.math.Vector2;
import tech.icey.r77.math.Vector3;

@Vertex
public record MyVertex(
        @Attribute(position = 0, name = "position") Vector2<Float> position,
        @Attribute(position = 1, name = "color") Vector3<Float> color
) {}

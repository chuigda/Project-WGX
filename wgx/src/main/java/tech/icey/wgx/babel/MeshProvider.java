package tech.icey.wgx.babel;

import tech.icey.r77.asset.Vertex;
import tech.icey.util.Pair;

import java.util.List;

public interface MeshProvider {
    boolean hasMeshUpdate();

    Pair<String, List<Vertex>> provideMesh();
}

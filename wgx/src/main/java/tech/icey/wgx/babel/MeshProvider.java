package tech.icey.wgx.babel;

import tech.icey.r77.asset.Vertex;

import java.util.List;

public interface MeshProvider {
    boolean hasMeshUpdate();

    List<Vertex> provideMesh();
}

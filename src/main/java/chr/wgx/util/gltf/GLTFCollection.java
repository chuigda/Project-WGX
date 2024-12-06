package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class GLTFCollection implements IPointer, AutoCloseable {
    private static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            ValueLayout.ADDRESS.withTargetLayout(GLTFMesh.LAYOUT), // models: *mut GLTFMesh
            ValueLayout.JAVA_LONG // model_count: u64
    );

    private final GLTF gltf;
    public final MemorySegment segment;

    GLTFCollection(GLTF gltf, MemorySegment segment) {
        this.gltf = gltf;
        this.segment = segment;
    }

    @Override
    public MemorySegment segment() {
        return segment;
    }

    @Override
    public void close() {
        gltf.free(this);
    }
}

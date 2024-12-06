package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;

public final class GLTFCollection implements IPointer, AutoCloseable {
    private final GLTF gltf;
    public final MemorySegment segment;

    GLTFCollection(GLTF gltf, MemorySegment segment) {
        this.gltf = gltf;
        this.segment = segment.reinterpret(SIZE);
    }

    public long modelCount() {
        return segment.get(LAYOUT$model_count, OFFSET$model_count);
    }

    public List<GLTFMesh> models() {
        long count = modelCount();

        MemorySegment ptr = segment.get(LAYOUT$models, OFFSET$models).reinterpret(count * GLTFMesh.SIZE);
        List<GLTFMesh> ret = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            MemorySegment modelSegment = ptr.asSlice(i * GLTFMesh.SIZE, GLTFMesh.SIZE);
            ret.add(new GLTFMesh(modelSegment));
        }
        return ret;
    }

    @Override
    public MemorySegment segment() {
        return segment;
    }

    @Override
    public void close() {
        gltf.free(this);
    }

    static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            ValueLayout.ADDRESS.withTargetLayout(GLTFMesh.LAYOUT).withName("models"), // models: *mut GLTFMesh
            ValueLayout.JAVA_LONG.withName("model_count") // model_count: u64
    );
    static final long SIZE = LAYOUT.byteSize();

    private static final MemoryLayout.PathElement PATH$models = MemoryLayout.PathElement.groupElement("models");
    private static final MemoryLayout.PathElement PATH$model_count = MemoryLayout.PathElement.groupElement("model_count");

    private static final AddressLayout LAYOUT$models = (AddressLayout) LAYOUT.select(PATH$models);
    private static final ValueLayout.OfLong LAYOUT$model_count = (ValueLayout.OfLong) LAYOUT.select(PATH$model_count);

    private static final long OFFSET$models = LAYOUT.byteOffset(PATH$models);
    private static final long OFFSET$model_count = LAYOUT.byteOffset(PATH$model_count);
}

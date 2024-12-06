package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class GLTFMesh implements IPointer {
    public final MemorySegment segment;

    GLTFMesh(MemorySegment segment) {
        this.segment = segment;
    }

    public String modelName() {
        return segment.get(LAYOUT$model_name, OFFSET$model_name)
                .reinterpret(Long.MAX_VALUE)
                .getString(0);
    }

    public long vertexCount() {
        return segment.get(LAYOUT$vertex_count, OFFSET$vertex_count);
    }

    public MemorySegment vertices() {
        long count = vertexCount();
        return segment.get(LAYOUT$vertices, OFFSET$vertices).reinterpret(count * GLTFVertex.SIZE);
    }

    public long indexCount() {
        return segment.get(LAYOUT$index_count, OFFSET$index_count);
    }

    public MemorySegment indices() {
        long count = indexCount();
        return segment.get(LAYOUT$indices, OFFSET$indices).reinterpret(count * ValueLayout.JAVA_INT.byteSize());
    }

    @Override
    public MemorySegment segment() {
        return segment;
    }

    static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_BYTE).withName("model_name"), // model_name: *const c_char
            ValueLayout.ADDRESS.withTargetLayout(GLTFVertex.LAYOUT).withName("vertices"), // vertices: *mut GLTFVertex
            ValueLayout.JAVA_LONG.withName("vertex_count"), // vertex_count: u64
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT).withName("indices"), // indices: *mut u32
            ValueLayout.JAVA_LONG.withName("index_count") // index_count: u64
    );
    static final long SIZE = LAYOUT.byteSize();

    private static final MemoryLayout.PathElement PATH$model_name = MemoryLayout.PathElement.groupElement("model_name");
    private static final MemoryLayout.PathElement PATH$vertices = MemoryLayout.PathElement.groupElement("vertices");
    private static final MemoryLayout.PathElement PATH$vertex_count = MemoryLayout.PathElement.groupElement("vertex_count");
    private static final MemoryLayout.PathElement PATH$indices = MemoryLayout.PathElement.groupElement("indices");
    private static final MemoryLayout.PathElement PATH$index_count = MemoryLayout.PathElement.groupElement("index_count");

    private static final AddressLayout LAYOUT$model_name = (AddressLayout) LAYOUT.select(PATH$model_name);
    private static final AddressLayout LAYOUT$vertices = (AddressLayout) LAYOUT.select(PATH$vertices);
    private static final ValueLayout.OfLong LAYOUT$vertex_count = (ValueLayout.OfLong) LAYOUT.select(PATH$vertex_count);
    private static final AddressLayout LAYOUT$indices = (AddressLayout) LAYOUT.select(PATH$indices);
    private static final ValueLayout.OfLong LAYOUT$index_count = (ValueLayout.OfLong) LAYOUT.select(PATH$index_count);

    public static final long OFFSET$model_name = LAYOUT.byteOffset(PATH$model_name);
    public static final long OFFSET$vertices = LAYOUT.byteOffset(PATH$vertices);
    public static final long OFFSET$vertex_count = LAYOUT.byteOffset(PATH$vertex_count);
    public static final long OFFSET$indices = LAYOUT.byteOffset(PATH$indices);
    public static final long OFFSET$index_count = LAYOUT.byteOffset(PATH$index_count);


}

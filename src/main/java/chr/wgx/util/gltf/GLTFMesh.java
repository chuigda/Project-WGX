package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public record GLTFMesh(MemorySegment segment) implements IPointer {
    static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_BYTE), // model_name: *const c_char
            ValueLayout.ADDRESS.withTargetLayout(GLTFVertex.LAYOUT), // vertices: *mut GLTFVertex
            ValueLayout.JAVA_LONG, // vertex_count: u64
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT), // indices: *mut u32
            ValueLayout.JAVA_LONG // index_count: u64
    );
}

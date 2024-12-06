package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public record GLTFVertex(MemorySegment segment) implements IPointer {
    static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT).withName("position"), // position: [f32; 3]
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT).withName("normal"), // normal: [f32; 3]
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT).withName("tex_coord"), // tex_coord: [f32; 2]
            MemoryLayout.sequenceLayout(1, ValueLayout.JAVA_INT).withName("id")  // id: u32
    );
    static final long SIZE = LAYOUT.byteSize();
}

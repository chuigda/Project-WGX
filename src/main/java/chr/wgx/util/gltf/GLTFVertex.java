package chr.wgx.util.gltf;

import tech.icey.panama.IPointer;
import tech.icey.panama.NativeLayout;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public record GLTFVertex(MemorySegment segment) implements IPointer {
    static final MemoryLayout LAYOUT = NativeLayout.structLayout(
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT), // position: [f32; 3]
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT), // normal: [f32; 3]
            MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT), // tex_coord: [f32; 2]
            MemoryLayout.sequenceLayout(1, ValueLayout.JAVA_INT)  // id: u32
    );
}

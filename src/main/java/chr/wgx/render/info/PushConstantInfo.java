package chr.wgx.render.info;

import java.lang.foreign.MemoryLayout;
import java.util.ArrayList;
import java.util.List;

public final class PushConstantInfo {
    public final List<PushConstantRange> pushConstantRanges;
    public final MemoryLayout cpuLayout;
    public final int bufferSize;

    public PushConstantInfo(List<PushConstantRange> pushConstantRanges) {
        this.pushConstantRanges = pushConstantRanges;

        int currentOffset = 0;
        int maxAlignment = 0;

        List<MemoryLayout> paddedElements = new ArrayList<>();
        for (PushConstantRange range : pushConstantRanges) {
            int alignment = range.type.std140Alignment;
            if (maxAlignment <= alignment) {
                maxAlignment = alignment;
            }

            int padding = (alignment - (currentOffset % alignment)) % alignment;
            if (padding != 0) {
                paddedElements.add(MemoryLayout.paddingLayout(padding));
                currentOffset += padding;
            }

            assert currentOffset <= range.offset;
            if (currentOffset < range.offset) {
                paddedElements.add(MemoryLayout.paddingLayout(range.offset - currentOffset));
                currentOffset = range.offset;
            }

            paddedElements.add(range.type.cpuLayout);
            currentOffset += range.type.byteSize;
        }

        if (maxAlignment > 0) {
            int padding = (maxAlignment - (currentOffset % maxAlignment)) % maxAlignment;
            if (padding != 0) {
                paddedElements.add(MemoryLayout.paddingLayout(padding));
                currentOffset += padding;
            }
        }

        this.cpuLayout = MemoryLayout.structLayout(paddedElements.toArray(new MemoryLayout[0]));
        this.bufferSize = currentOffset;
    }
}

package chr.wgx.render.info;

import chr.wgx.render.common.ShaderStage;

import java.lang.foreign.MemoryLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class UniformBufferBindingInfo extends DescriptorLayoutBindingInfo {
    public final List<FieldInfo> fields;
    public final HashMap<String, FieldInfo> fieldMap;
    public final MemoryLayout cpuLayout;
    public final int bufferSize;

    public UniformBufferBindingInfo(List<FieldInfoInput> fieldInfoInputs, ShaderStage stage) {
        super(DescriptorType.UNIFORM_BUFFER, stage);

        // 用于计算 std140 布局中的对齐
        int currentLocation = 0;
        int currentOffset = 0;

        // 用于计算在 CPU 上分配内存时，整块内存需要的对齐
        int maxAlignmentCPU = 0;

        List<MemoryLayout> paddedElements = new ArrayList<>();

        this.fields = new ArrayList<>();
        this.fieldMap = new HashMap<>();

        for (FieldInfoInput fieldInfoInput : fieldInfoInputs) {
            int alignment = fieldInfoInput.type.std140Alignment;
            int cpuAlignment = fieldInfoInput.type.cpuAlignment;

            if (maxAlignmentCPU <= cpuAlignment) {
                maxAlignmentCPU = cpuAlignment;
            }

            int padding = (alignment - (currentOffset % alignment)) % alignment;
            if (padding != 0) {
                paddedElements.add(MemoryLayout.paddingLayout(padding));
                currentOffset += padding;
            }

            FieldInfo fieldInfo = new FieldInfo(
                    fieldInfoInput.name,
                    fieldInfoInput.type,
                    currentLocation,
                    currentOffset
            );
            this.fields.add(fieldInfo);
            this.fieldMap.put(fieldInfoInput.name, fieldInfo);
            paddedElements.add(fieldInfo.type.cpuLayout);

            currentOffset += fieldInfoInput.type.byteSize;
        }

        if (maxAlignmentCPU > 0) {
            int padding = (maxAlignmentCPU - (currentOffset % maxAlignmentCPU)) % maxAlignmentCPU;
            if (padding != 0) {
                paddedElements.add(MemoryLayout.paddingLayout(padding));
                currentOffset += padding;
            }
        }

        this.cpuLayout = MemoryLayout.structLayout(paddedElements.toArray(new MemoryLayout[0]));
        this.bufferSize = currentOffset;
    }
}

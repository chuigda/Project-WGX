package chr.wgx.render.info;

import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.xjbutil.container.Either;

import java.util.List;

public record RenderPipelineCreateInfo(
        List<DescriptorInfo> descriptorSetLayout,
        Either<String, ByteBuffer> vertexShader,
        Either<String, ByteBuffer> fragmentShader
) {}

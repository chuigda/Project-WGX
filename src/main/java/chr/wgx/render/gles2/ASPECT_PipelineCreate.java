package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.DescriptorSetLayout;
import chr.wgx.render.gles2.data.GLES2RenderPipeline;
import chr.wgx.render.gles2.data.UniformLocation;
import chr.wgx.render.info.*;
import tech.icey.gles2.GLES2;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;

public final class ASPECT_PipelineCreate {
    ASPECT_PipelineCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    GLES2RenderPipeline createPipelineImpl(RenderPipelineCreateInfo createInfo) throws RenderException{
        GLES2 gles2 = engine.gles2;

        if (!(createInfo.gles2ShaderProgram instanceof Option.Some<ShaderProgram.GLES2> someShaderProgram)) {
            throw new RenderException("未提供 GLES2 渲染器所需的着色器程序");
        }
        ShaderProgram.GLES2 shaderProgram = someShaderProgram.value;

        int program = GLES2Util.compileShaderProgram(gles2, shaderProgram.vertexShader, shaderProgram.fragmentShader);
        try (Arena arena = Arena.ofConfined()) {
            List<UniformLocation> uniformLocations = new ArrayList<>();
            for (DescriptorSetLayout layout : createInfo.descriptorSetLayouts) {
                for (DescriptorLayoutBindingInfo bindingInfo : layout.info.bindings) {
                    switch (bindingInfo) {
                        case TextureBindingInfo textureBindingInfo -> {
                            int location = gles2.glGetUniformLocation(
                                    program,
                                    ByteBuffer.allocateString(arena, textureBindingInfo.bindingName)
                            );
                            if (location == -1) {
                                throw new RenderException("未找到纹理绑定点: " + textureBindingInfo.bindingName);
                            }

                            uniformLocations.add(new UniformLocation(textureBindingInfo.bindingName, location));
                        }
                        case UniformBufferBindingInfo uniformBufferBindingInfo -> {
                            for (FieldInfo fieldInfo : uniformBufferBindingInfo.fields) {
                                String uniformFullName = uniformBufferBindingInfo.bindingName + "_" + fieldInfo.name;
                                int location = gles2.glGetUniformLocation(
                                        program,
                                        ByteBuffer.allocateString(arena, uniformFullName)
                                );
                                if (location == -1) {
                                    throw new RenderException("未找到统一缓冲区绑定点: " + uniformFullName);
                                }

                                uniformLocations.add(new UniformLocation(uniformFullName, location));
                            }
                        }
                    }
                }
            }

            if (createInfo.pushConstantInfo instanceof Option.Some<PushConstantInfo> somePushConstantInfo) {
                for (PushConstantRange range : somePushConstantInfo.value.pushConstantRanges) {
                    String uniformFullName = "pco_" + range.name;
                    int location = gles2.glGetUniformLocation(
                            program,
                            ByteBuffer.allocateString(arena, uniformFullName)
                    );
                    if (location == -1) {
                        throw new RenderException("未找到推送常量绑定点: " + uniformFullName);
                    }

                    uniformLocations.add(new UniformLocation(uniformFullName, location));
                }
            }

            GLES2RenderPipeline ret = new GLES2RenderPipeline(createInfo, program, uniformLocations);
            engine.pipelines.add(ret);
            return ret;
        }
    }

    private final GLES2RenderEngine engine;
}

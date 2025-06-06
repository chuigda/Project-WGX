package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.DescriptorSetLayout;
import chr.wgx.render.gles2.data.AttributeBinding;
import chr.wgx.render.gles2.data.GLES2RenderPipeline;
import chr.wgx.render.gles2.data.UniformLocation;
import chr.wgx.render.info.*;
import club.doki7.gles2.GLES2;
import club.doki7.ffm.ptr.BytePtr;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ASPECT_PipelineCreate {
    ASPECT_PipelineCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    GLES2RenderPipeline createPipelineImpl(RenderPipelineCreateInfo createInfo) throws RenderException {
        GLES2 gles2 = engine.gles2;

        if (!(createInfo.gles2ShaderProgram instanceof Option.Some<ShaderProgram.GLES2> someShaderProgram)) {
            throw new RenderException("未提供 GLES2 渲染器所需的着色器程序");
        }
        ShaderProgram.GLES2 shaderProgram = someShaderProgram.value;

        int program = GLES2Util.compileShaderProgram(gles2, shaderProgram.vertexShader, shaderProgram.fragmentShader);
        try (Arena arena = Arena.ofConfined()) {
            // TODO make use of this attributeBindings, instead of restricting the ways writing GLSL
            List<AttributeBinding> attributeBindings = new ArrayList<>();
            for (FieldInfo attributeInfo : createInfo.vertexInputInfo.attributes) {
                String actualAttributeName = makeAttributeName(attributeInfo.name);
                int location = gles2.getAttribLocation(
                        program,
                        BytePtr.allocateString(arena, actualAttributeName)
                );
                if (location == -1) {
                    throw new RenderException("未找到属性绑定点: " + attributeInfo.name + "(" + actualAttributeName + ")");
                }

                attributeBindings.add(new AttributeBinding(attributeInfo.name, location));
            }

            List<UniformLocation> uniformLocations = new ArrayList<>();
            for (DescriptorSetLayout layout : createInfo.descriptorSetLayouts) {
                for (DescriptorLayoutBindingInfo bindingInfo : layout.createInfo.bindings) {
                    switch (bindingInfo) {
                        case TextureBindingInfo textureBindingInfo -> {
                            int location = gles2.getUniformLocation(
                                    program,
                                    BytePtr.allocateString(arena, textureBindingInfo.bindingName)
                            );
                            if (location == -1) {
                                logger.warning("未找到纹理绑定点: " + textureBindingInfo.bindingName + ", 这可能是因为纹理未被着色器使用。着色器程序仍然可以运行，但纹理将不会被更新");
                            }

                            uniformLocations.add(new UniformLocation(textureBindingInfo.bindingName, location));
                        }
                        case UniformBufferBindingInfo uniformBufferBindingInfo -> {
                            for (FieldInfo fieldInfo : uniformBufferBindingInfo.fields) {
                                String uniformFullName = uniformBufferBindingInfo.bindingName + "_" + fieldInfo.name;
                                int location = gles2.getUniformLocation(
                                        program,
                                        BytePtr.allocateString(arena, uniformFullName)
                                );
                                if (location == -1) {
                                    logger.warning("未找到统一缓冲区绑定点: " + uniformFullName + ", 这可能是因为统一缓冲区未被着色器使用。着色器程序仍然可以运行，但统一缓冲区将不会被更新");
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
                    int location = gles2.getUniformLocation(
                            program,
                            BytePtr.allocateString(arena, uniformFullName)
                    );
                    if (location == -1) {
                        logger.warning("未找到推送常量绑定点: " + uniformFullName + ", 这可能是因为推送常量未被着色器使用。着色器程序仍然可以运行，但推送常量将不会被更新");
                    }

                    uniformLocations.add(new UniformLocation(uniformFullName, location));
                }
            }

            GLES2RenderPipeline ret = new GLES2RenderPipeline(createInfo, program, uniformLocations);
            engine.pipelines.add(ret);
            return ret;
        }
    }

    private static String makeAttributeName(String name) {
        if (name.startsWith("a") && name.length() >= 2 && isCapital(name.charAt(1))) {
            return name;
        } else {
            return "a" + name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    private static boolean isCapital(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    private final GLES2RenderEngine engine;
    private static final Logger logger = Logger.getLogger(ASPECT_PipelineCreate.class.getName());
}

package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.DescriptorSetLayout;
import chr.wgx.render.gles2.data.GLES2RenderPipeline;
import chr.wgx.render.gles2.data.UniformLocation;
import chr.wgx.render.info.*;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
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

        int vertexShader = gles2.glCreateShader(GLES2Constants.GL_VERTEX_SHADER);
        int fragmentShader = gles2.glCreateShader(GLES2Constants.GL_FRAGMENT_SHADER);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment vertexShaderSource = arena.allocateFrom(shaderProgram.vertexShader);
            MemorySegment fragmentShaderSource = arena.allocateFrom(shaderProgram.fragmentShader);
            PointerBuffer pVertexShaderSource = PointerBuffer.allocate(arena);
            PointerBuffer pFragmentShaderSource = PointerBuffer.allocate(arena);
            pVertexShaderSource.write(vertexShaderSource);
            pFragmentShaderSource.write(fragmentShaderSource);

            gles2.glShaderSource(vertexShader, 1, pVertexShaderSource, null);
            gles2.glShaderSource(fragmentShader, 1, pFragmentShaderSource, null);

            IntBuffer pStatus = IntBuffer.allocate(arena);

            gles2.glCompileShader(vertexShader);
            gles2.glGetShaderiv(vertexShader, GLES2Constants.GL_COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetShaderiv(vertexShader, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetShaderInfoLog(vertexShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("顶点着色器编译失败: " + infoLog.readString());
            }

            gles2.glCompileShader(fragmentShader);
            gles2.glGetShaderiv(fragmentShader, GLES2Constants.GL_COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetShaderiv(fragmentShader, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetShaderInfoLog(fragmentShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("片段着色器编译失败: " + infoLog.readString());
            }

            int program = gles2.glCreateProgram();
            gles2.glAttachShader(program, vertexShader);
            gles2.glAttachShader(program, fragmentShader);
            gles2.glLinkProgram(program);
            gles2.glGetProgramiv(program, GLES2Constants.GL_LINK_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetProgramiv(program, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetProgramInfoLog(program, infoLogLength, pStatus, infoLog);

                gles2.glDeleteProgram(program);
                throw new RenderException("着色器程序链接失败: " + infoLog.readString());
            }

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
        } finally {
            gles2.glDeleteShader(vertexShader);
            gles2.glDeleteShader(fragmentShader);
        }
    }

    private final GLES2RenderEngine engine;
}

package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.data.Descriptor;
import chr.wgx.render.data.Texture;
import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.gles2.data.*;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import chr.wgx.render.gles2.task.GLES2RenderPipelineBind;
import chr.wgx.render.gles2.task.GLES2RenderTask;
import chr.wgx.render.gles2.task.GLES2RenderTaskGroup;
import chr.wgx.render.info.FieldInfo;
import chr.wgx.render.info.PushConstantRange;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.buffer.FloatBuffer;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class ASPECT_RenderFrame {
    ASPECT_RenderFrame(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    void renderFrameImpl() throws RenderException {
        GLES2 gles2 = engine.gles2;

        for (GLES2RenderPass renderPass : engine.renderPasses) {
            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, renderPass.framebufferObject);
            gles2.glClear(GLES2Constants.GL_COLOR_BUFFER_BIT | GLES2Constants.GL_DEPTH_BUFFER_BIT);

            for (GLES2RenderPipelineBind pipelineBind : renderPass.bindList) {
                gles2.glUseProgram(pipelineBind.pipeline.shaderProgram);

                for (GLES2RenderTaskGroup renderTaskGroup : pipelineBind.renderTaskGroups) {
                    int offset0 = 0;
                    int textureUnitCount0 = 0;
                    for (GLES2DescriptorSet descriptorSet : renderTaskGroup.sharedDescriptorSets) {
                        for (Descriptor descriptor : descriptorSet.createInfo.descriptors) {
                            switch (descriptor) {
                                case Texture texture -> {
                                    GLES2Texture gles2Texture = (GLES2Texture) texture;
                                    UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset0);
                                    gles2.glActiveTexture(GLES2Constants.GL_TEXTURE0 + textureUnitCount0);
                                    gles2.glBindTexture(GLES2Constants.GL_TEXTURE_2D, gles2Texture.textureObject);
                                    gles2.glUniform1i(uniformLocation.location, textureUnitCount0);

                                    offset0++;
                                    textureUnitCount0++;
                                }
                                case UniformBuffer uniformBuffer -> {
                                    GLES2UniformBuffer gles2UniformBuffer = (GLES2UniformBuffer) uniformBuffer;
                                    synchronized (gles2UniformBuffer.cpuBuffer) {
                                        for (FieldInfo field : gles2UniformBuffer.createInfo.bindingInfo.fields) {
                                            UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset0);
                                            uploadUniformData(
                                                    gles2,
                                                    uniformLocation,
                                                    field.type,
                                                    field.byteOffset,
                                                    gles2UniformBuffer.cpuBuffer
                                            );
                                            offset0++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (GLES2RenderTask renderTask : renderTaskGroup.renderTasks) {
                        int offset1 = offset0;
                        int textureUnitCount1 = textureUnitCount0;
                        for (GLES2DescriptorSet descriptorSet : renderTask.descriptorSets) {
                            for (Descriptor descriptor : descriptorSet.createInfo.descriptors) {
                                switch (descriptor) {
                                    case Texture texture -> {
                                        GLES2Texture gles2Texture = (GLES2Texture) texture;
                                        UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset1);
                                        gles2.glActiveTexture(GLES2Constants.GL_TEXTURE0 + textureUnitCount1);
                                        gles2.glBindTexture(GLES2Constants.GL_TEXTURE_2D, gles2Texture.textureObject);
                                        gles2.glUniform1i(uniformLocation.location, textureUnitCount1);

                                        offset1++;
                                        textureUnitCount1++;
                                    }
                                    case UniformBuffer uniformBuffer -> {
                                        GLES2UniformBuffer gles2UniformBuffer = (GLES2UniformBuffer) uniformBuffer;
                                        synchronized (gles2UniformBuffer.cpuBuffer) {
                                            for (FieldInfo field : gles2UniformBuffer.createInfo.bindingInfo.fields) {
                                                UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset1);
                                                uploadUniformData(
                                                        gles2,
                                                        uniformLocation,
                                                        field.type,
                                                        field.byteOffset,
                                                        gles2UniformBuffer.cpuBuffer
                                                );
                                                offset1++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (renderTask.pushConstant instanceof Option.Some<GLES2PushConstant> some) {
                            GLES2PushConstant pushConstant = some.value;
                            for (PushConstantRange range : pushConstant.info.pushConstantRanges) {
                                UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset1);
                                uploadUniformData(
                                        gles2,
                                        uniformLocation,
                                        range.type,
                                        range.offset,
                                        pushConstant.cpuBuffer
                                );
                                offset1++;
                            }
                        }

                        for (FieldInfo vertexInput : pipelineBind.pipeline.createInfo.vertexInputInfo.attributes) {
                            int location = vertexInput.location;
                            int byteOffset = vertexInput.byteOffset;

                            gles2.glEnableVertexAttribArray(location);
                            gles2.glVertexAttribPointer(
                                    location,
                                    vertexInput.type.componentCount,
                                    vertexInput.type == CGType.Int ? GLES2Constants.GL_INT : GLES2Constants.GL_FLOAT,
                                    (byte) 0,
                                    pipelineBind.pipeline.createInfo.vertexInputInfo.stride,
                                    MemorySegment.ofAddress(byteOffset)
                            );
                        }

                        // bind vertex buffer
                        gles2.glBindBuffer(GLES2Constants.GL_ARRAY_BUFFER, renderTask.renderObject.vertexVBO);
                        // bind index buffer
                        gles2.glBindBuffer(GLES2Constants.GL_ELEMENT_ARRAY_BUFFER, renderTask.renderObject.indexVBO);
                        // draw
                        gles2.glDrawElements(
                                GLES2Constants.GL_TRIANGLES,
                                renderTask.renderObject.indexCount,
                                GLES2Constants.GL_UNSIGNED_INT,
                                MemorySegment.NULL
                        );
                    }
                }
            }
        }

        int status = gles2.glGetError();
        if (status != GLES2Constants.GL_NO_ERROR) {
            throw new RenderException("OpenGL 错误: " + status);
        }
    }

    private void uploadUniformData(
            GLES2 gles2,
            UniformLocation uniformLocation,
            CGType type,
            long byteOffset,
            MemorySegment cpuBuffer
    ) {
        switch (type) {
            case Float -> {
                float value = cpuBuffer.get(ValueLayout.JAVA_FLOAT, byteOffset);
                gles2.glUniform1f(uniformLocation.location, value);
            }
            case Int -> {
                int value = cpuBuffer.get(ValueLayout.JAVA_INT, byteOffset);
                gles2.glUniform1i(uniformLocation.location, value);
            }
            case Vec2 -> gles2.glUniform2fv(
                    uniformLocation.location,
                    1,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
            case Vec3 -> gles2.glUniform3fv(
                    uniformLocation.location,
                    1,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
            case Vec4 -> gles2.glUniform4fv(
                    uniformLocation.location,
                    1,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
            case Mat2 -> gles2.glUniformMatrix2fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
            case Mat3 -> gles2.glUniformMatrix3fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
            case Mat4 -> gles2.glUniformMatrix4fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatBuffer(cpuBuffer.asSlice(byteOffset))
            );
        }
    }

    private final GLES2RenderEngine engine;
}

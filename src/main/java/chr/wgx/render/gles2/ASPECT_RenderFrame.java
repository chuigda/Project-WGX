package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ClearBehavior;
import chr.wgx.render.common.Color;
import chr.wgx.render.data.Attachment;
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
import chr.wgx.render.info.RenderPassAttachmentInfo;
import org.intellij.lang.annotations.Language;
import club.doki7.gles2.GLES2;
import club.doki7.gles2.GLES2Constants;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.FloatPtr;
import club.doki7.ffm.ptr.IntPtr;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.HashSet;

public final class ASPECT_RenderFrame {
    ASPECT_RenderFrame(GLES2RenderEngine engine) throws RenderException {
        this.engine = engine;
        GLES2 gles2 = engine.gles2;

        hiddenShaderProgram = GLES2Util.compileShaderProgram(
                gles2,
                HIDDEN_PASS_VERTEX_SHADER,
                HIDDEN_PASS_FRAGMENT_SHADER
        );

        try (Arena arena = Arena.ofConfined()) {
            FloatPtr vertexBufferSegment = FloatPtr.allocate(arena, HIDDEN_OBJECT_VERTICES);
            IntPtr indexBufferSegment = IntPtr.allocate(arena, HIDDEN_OBJECT_INDICES);

            IntPtr pVBO = IntPtr.allocate(arena, 2);
            gles2.genBuffers(2, pVBO);

            hiddenObjectVBO = pVBO.read();
            hiddenObjectIBO = pVBO.read(1);

            gles2.bindBuffer(GLES2Constants.ARRAY_BUFFER, hiddenObjectVBO);
            gles2.bufferData(
                    GLES2Constants.ARRAY_BUFFER,
                    vertexBufferSegment.segment().byteSize(),
                    vertexBufferSegment.segment(),
                    GLES2Constants.STATIC_DRAW
            );

            gles2.bindBuffer(GLES2Constants.ELEMENT_ARRAY_BUFFER, hiddenObjectIBO);
            gles2.bufferData(
                    GLES2Constants.ELEMENT_ARRAY_BUFFER,
                    indexBufferSegment.segment().byteSize(),
                    indexBufferSegment.segment(),
                    GLES2Constants.STATIC_DRAW
            );
        }

        pDefaultFramebuffer = IntPtr.allocate(engine.prefabArena);
    }

    void renderFrameImpl() throws RenderException {
        HashSet<Attachment> clearedAttachments = new HashSet<>();

        GLES2 gles2 = engine.gles2;
        gles2.disable(GLES2Constants.CULL_FACE);
        gles2.disable(GLES2Constants.BLEND);

        gles2.getIntegerv(GLES2Constants.FRAMEBUFFER_BINDING, pDefaultFramebuffer);
        int defaultFramebuffer = pDefaultFramebuffer.read();

        for (GLES2RenderPass renderPass : engine.renderPasses) {
             gles2.bindFramebuffer(GLES2Constants.FRAMEBUFFER, renderPass.framebufferObject);

            Attachment firstAttachment = renderPass.colorAttachments.getFirst();
            int actualWidth = firstAttachment.createInfo.width == -1
                    ? engine.framebufferWidth
                    : firstAttachment.createInfo.width;
            int actualHeight = firstAttachment.createInfo.height == -1
                    ? engine.framebufferHeight
                    : firstAttachment.createInfo.height;
            gles2.viewport(0, 0, actualWidth, actualHeight);

            // TODO find a way to support multiple clear colors
            RenderPassAttachmentInfo renderPassAttachmentInfo = renderPass.info.colorAttachmentInfos.getFirst();
            if (renderPassAttachmentInfo.clearBehavior == ClearBehavior.CLEAR_ALWAYS
                || !clearedAttachments.contains(firstAttachment)) {
                Color clearColor = renderPassAttachmentInfo.clearColor;
                gles2.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
                gles2.clear(GLES2Constants.COLOR_BUFFER);
                clearedAttachments.add(firstAttachment);
            }
            if (renderPass.info.depthAttachmentInfo instanceof Option.Some<RenderPassAttachmentInfo> some) {
                RenderPassAttachmentInfo depthAttachmentInfo = some.value;
                Attachment depthAttachment = some.value.attachment;

                if (depthAttachmentInfo.clearBehavior == ClearBehavior.CLEAR_ALWAYS
                    || !clearedAttachments.contains(depthAttachment)) {
                    gles2.clear(GLES2Constants.DEPTH_BUFFER);
                    clearedAttachments.add(depthAttachment);
                }
            }

            for (GLES2RenderPipelineBind pipelineBind : renderPass.bindList) {
                gles2.useProgram(pipelineBind.pipeline.shaderProgram);
                if (pipelineBind.pipeline.createInfo.depthTest) {
                    gles2.enable(GLES2Constants.DEPTH_TEST);
                } else {
                    gles2.disable(GLES2Constants.DEPTH_TEST);
                }

                for (GLES2RenderTaskGroup renderTaskGroup : pipelineBind.renderTaskGroups) {
                    int offset0 = 0;
                    int textureUnitCount0 = 0;
                    for (GLES2DescriptorSet descriptorSet : renderTaskGroup.sharedDescriptorSets) {
                        for (Descriptor descriptor : descriptorSet.createInfo.descriptors) {
                            switch (descriptor) {
                                case Texture texture -> {
                                    GLES2Texture gles2Texture = (GLES2Texture) texture;
                                    UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset0);
                                    offset0++;
                                    if (uniformLocation.location < 0) {
                                        continue;
                                    }
                                    gles2.activeTexture(GLES2Constants.TEXTURE0 + textureUnitCount0);
                                    gles2.bindTexture(GLES2Constants.TEXTURE_2D, gles2Texture.textureObject);
                                    gles2.uniform1i(uniformLocation.location, textureUnitCount0);

                                    textureUnitCount0++;
                                }
                                case UniformBuffer uniformBuffer -> {
                                    GLES2UniformBuffer gles2UniformBuffer = (GLES2UniformBuffer) uniformBuffer;
                                    synchronized (gles2UniformBuffer.cpuBuffer) {
                                        for (FieldInfo field : gles2UniformBuffer.createInfo.bindingInfo.fields) {
                                            UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset0);
                                            offset0++;
                                            if (uniformLocation.location < 0) {
                                                continue;
                                            }
                                            uploadUniformData(
                                                    gles2,
                                                    uniformLocation,
                                                    field.type,
                                                    field.byteOffset,
                                                    gles2UniformBuffer.cpuBuffer
                                            );
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
                                        offset1++;
                                        if (uniformLocation.location < 0) {
                                            continue;
                                        }
                                        gles2.activeTexture(GLES2Constants.TEXTURE0 + textureUnitCount1);
                                        gles2.bindTexture(GLES2Constants.TEXTURE_2D, gles2Texture.textureObject);
                                        gles2.uniform1i(uniformLocation.location, textureUnitCount1);

                                        textureUnitCount1++;
                                    }
                                    case UniformBuffer uniformBuffer -> {
                                        GLES2UniformBuffer gles2UniformBuffer = (GLES2UniformBuffer) uniformBuffer;
                                        synchronized (gles2UniformBuffer.cpuBuffer) {
                                            for (FieldInfo field : gles2UniformBuffer.createInfo.bindingInfo.fields) {
                                                UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset1);
                                                if (uniformLocation.location < 0) {
                                                    continue;
                                                }
                                                offset1++;
                                                uploadUniformData(
                                                        gles2,
                                                        uniformLocation,
                                                        field.type,
                                                        field.byteOffset,
                                                        gles2UniformBuffer.cpuBuffer
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (renderTask.pushConstant instanceof Option.Some<GLES2PushConstant> some) {
                            GLES2PushConstant pushConstant = some.value;
                            for (PushConstantRange range : pushConstant.createInfo.pushConstantRanges) {
                                UniformLocation uniformLocation = pipelineBind.pipeline.uniformLocations.get(offset1);
                                offset1++;
                                if (uniformLocation.location < 0) {
                                    continue;
                                }
                                uploadUniformData(
                                        gles2,
                                        uniformLocation,
                                        range.type,
                                        range.offset,
                                        pushConstant.cpuBuffer
                                );
                            }
                        }

                        gles2.bindBuffer(GLES2Constants.ELEMENT_ARRAY_BUFFER, renderTask.renderObject.indexVBO);
                        gles2.bindBuffer(GLES2Constants.ARRAY_BUFFER, renderTask.renderObject.vertexVBO);

                        for (FieldInfo vertexInput : pipelineBind.pipeline.createInfo.vertexInputInfo.attributes) {
                            int location = vertexInput.location;
                            int byteOffset = vertexInput.byteOffset;

                            gles2.enableVertexAttribArray(location);
                            gles2.vertexAttribPointer(
                                    location,
                                    vertexInput.type.componentCount,
                                    vertexInput.type == CGType.Int ? GLES2Constants.INT : GLES2Constants.FLOAT,
                                    (byte) 0,
                                    pipelineBind.pipeline.createInfo.vertexInputInfo.stride,
                                    MemorySegment.ofAddress(byteOffset)
                            );
                        }

                        gles2.drawElements(
                                GLES2Constants.TRIANGLES,
                                renderTask.renderObject.indexCount,
                                GLES2Constants.UNSIGNED_INT,
                                MemorySegment.NULL
                        );
                    }
                }
            }
        }

        gles2.bindFramebuffer(GLES2Constants.FRAMEBUFFER, defaultFramebuffer);
        gles2.viewport(0, 0, engine.framebufferWidth, engine.framebufferHeight);
        gles2.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gles2.clear(GLES2Constants.COLOR_BUFFER);
        gles2.disable(GLES2Constants.DEPTH_TEST);
        gles2.disable(GLES2Constants.BLEND);
        gles2.useProgram(hiddenShaderProgram);
        gles2.bindBuffer(GLES2Constants.ARRAY_BUFFER, hiddenObjectVBO);
        gles2.bindBuffer(GLES2Constants.ELEMENT_ARRAY_BUFFER, hiddenObjectIBO);
        gles2.enableVertexAttribArray(0);
        gles2.vertexAttribPointer(0, 2, GLES2Constants.FLOAT, (byte) 0, 16, MemorySegment.NULL);
        gles2.enableVertexAttribArray(1);
        gles2.vertexAttribPointer(1, 2, GLES2Constants.FLOAT, (byte) 0, 16, MemorySegment.ofAddress(2 * Float.BYTES));
        gles2.activeTexture(GLES2Constants.TEXTURE0);
        gles2.bindTexture(GLES2Constants.TEXTURE_2D, engine.defaultColorAttachmentTexture.textureObject);
        gles2.uniform1i(0, 0);
        gles2.drawElements(GLES2Constants.TRIANGLES, 6, GLES2Constants.UNSIGNED_INT, MemorySegment.NULL);

        int status = gles2.getError();
        if (status != GLES2Constants.NO_ERROR) {
            throw new RenderException("OpenGL 错误: " + status);
        }
    }

    private void uploadUniformData(
            GLES2 gles2,
            UniformLocation uniformLocation,
            CGType type,
            long byteOffset,
            MemorySegment cpuBuffer
    ) throws RenderException {
        assert cpuBuffer.isNative();

        switch (type) {
            case Float -> {
                float value = cpuBuffer.get(ValueLayout.JAVA_FLOAT, byteOffset);
                gles2.uniform1f(uniformLocation.location, value);
            }
            case Int -> {
                int value = cpuBuffer.get(ValueLayout.JAVA_INT, byteOffset);
                gles2.uniform1i(uniformLocation.location, value);
            }
            case Vec2 -> gles2.uniform2fv(
                    uniformLocation.location,
                    1,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
            case Vec3 -> gles2.uniform3fv(
                    uniformLocation.location,
                    1,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
            case Vec4 -> gles2.uniform4fv(
                    uniformLocation.location,
                    1,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
            case Mat2 -> gles2.uniformMatrix2fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
            case Mat3 -> gles2.uniformMatrix3fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
            case Mat4 -> gles2.uniformMatrix4fv(
                    uniformLocation.location,
                    1,
                    (byte) 0,
                    new FloatPtr(cpuBuffer.asSlice(byteOffset))
            );
        }

        @EnumType(GLES2Constants.class) int status = gles2.getError();
        if (status != GLES2Constants.NO_ERROR) {
            throw new RenderException("OpenGL 错误: " + status);
        }
    }

    private final GLES2RenderEngine engine;

    private final int hiddenShaderProgram;
    private final int hiddenObjectVBO;
    private final int hiddenObjectIBO;
    private final IntPtr pDefaultFramebuffer;

    private static final float[] HIDDEN_OBJECT_VERTICES = new float[] {
            // vec2 position, vec2 texCoord
            -1.0f, -1.0f,     0.0f, 0.0f,
            1.0f, -1.0f,      1.0f, 0.0f,
            1.0f,  1.0f,      1.0f, 1.0f,
            -1.0f,  1.0f,     0.0f, 1.0f
    };

    private static final int[] HIDDEN_OBJECT_INDICES = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    @Language("Glsl")
    private static final String HIDDEN_PASS_VERTEX_SHADER = """
            #version 100
            
            precision mediump float;
            
            attribute vec2 aPosition;
            attribute vec2 aTexCoord;
            
            varying vec2 vTexCoord;
            
            void main() {
                gl_Position = vec4(aPosition, 0.0, 1.0);
                vTexCoord = aTexCoord;
            }
            """;

    @Language("Glsl")
    private static final String HIDDEN_PASS_FRAGMENT_SHADER = """
            #version 100
            
            precision mediump float;
            
            varying vec2 vTexCoord;
            
            uniform sampler2D uTexture;
            
            const float SRGB_GAMMA = 1.0 / 2.2;
            const float SRGB_INVERSE_GAMMA = 2.2;
            const float SRGB_ALPHA = 0.055;
            
            float linear_to_srgb(float channel) {
                if(channel <= 0.0031308)
                    return 12.92 * channel;
                else
                    return (1.0 + SRGB_ALPHA) * pow(channel, 1.0/2.4) - SRGB_ALPHA;
            }
            
            vec3 rgb_to_srgb(vec3 rgb) {
                return vec3(
                    linear_to_srgb(rgb.r),
                    linear_to_srgb(rgb.g),
                    linear_to_srgb(rgb.b)
                );
            }
            
            void main() {
                vec4 texColor = texture2D(uTexture, vTexCoord);
                vec3 srgb = rgb_to_srgb(texColor.xyz);
                gl_FragColor = vec4(srgb, texColor.a);
            }
            """;
}

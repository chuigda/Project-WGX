package chr.wgx.drill;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.*;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.util.ColorUtil;
import chr.wgx.util.ImageUtil;
import chr.wgx.util.ResourceUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.w3c.dom.Text;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.logging.Logger;

public class DrillCreateObject {
    public static void createObjectInThread(AbstractRenderEngine engine) {
        new Thread(() -> {
            try {
                byte[] vertexShader1 = ResourceUtil.readBinaryFile("/resources/shader/vk/drill.vert.spv");
                byte[] fragmentShader1 = ResourceUtil.readBinaryFile("/resources/shader/vk/drill.frag.spv");
                byte[] vertexShader2 = ResourceUtil.readBinaryFile("/resources/shader/vk/drill2.vert.spv");
                byte[] fragmentShader2 = ResourceUtil.readBinaryFile("/resources/shader/vk/drill2.frag.spv");

                ShaderProgram.Vulkan shaderProgram1 = new ShaderProgram.Vulkan(vertexShader1, fragmentShader1);
                ShaderProgram.Vulkan shaderProgram2 = new ShaderProgram.Vulkan(vertexShader2, fragmentShader2);

                VertexInputInfo vertexInputInfo1 = new VertexInputInfo(List.of(
                        new FieldInfoInput("inPosition", CGType.Vec2),
                        new FieldInfoInput("inColor", CGType.Vec3)
                ));
                VertexInputInfo vertexInputInfo2 = new VertexInputInfo(List.of(
                        new FieldInfoInput("inPosition", CGType.Vec3),
                        new FieldInfoInput("inTexCoord", CGType.Vec2)
                ));

                RenderPipeline pipeline1 = engine.createPipeline(new RenderPipelineCreateInfo(
                        vertexInputInfo1,
                        List.of(),
                        List.of(),
                        Option.some(shaderProgram1),
                        Option.none(),
                        1,
                        false
                ));

                UniformBufferBindingInfo ubBindingInfo = new UniformBufferBindingInfo(List.of(
                        new FieldInfoInput("model", CGType.Mat4),
                        new FieldInfoInput("view", CGType.Mat4),
                        new FieldInfoInput("projection", CGType.Mat4)
                ), ShaderStage.VERTEX);
                TextureBindingInfo texBindingInfo = new TextureBindingInfo(ShaderStage.FRAGMENT);
                DescriptorSetLayout descriptorSetLayout = engine.createDescriptorSetLayout(
                        new DescriptorSetLayoutCreateInfo(List.of(ubBindingInfo, texBindingInfo)),
                        4
                );

                RenderPipeline pipeline2 = engine.createPipeline(new RenderPipelineCreateInfo(
                        vertexInputInfo2,
                        List.of(descriptorSetLayout),
                        List.of(),
                        Option.some(shaderProgram2),
                        Option.none(),
                        1,
                        true
                ));

                UniformBuffer ubo = engine.createUniform(new UniformBufferCreateInfo(
                        UniformUpdateFrequency.PER_FRAME,
                        ubBindingInfo
                ));
                ubo.updateBufferContent(MemorySegment.ofArray(new float[] {
                        1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f,

                        1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f,

                        1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f
                }));
                Pair<Attachment, Texture> rttTarget = engine.createColorAttachment(new AttachmentCreateInfo(
                        PixelFormat.RGBA8888_FLOAT,
                        640,
                        640
                ));
                Attachment rttTargetAttachment = rttTarget.first();
                Texture rttTexture = rttTarget.second();
                DescriptorSet descriptorSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                        descriptorSetLayout,
                        List.of(ubo, rttTexture)
                ));

                Pair<Attachment, Attachment> defaultAttachments = engine.getDefaultAttachments();
                Attachment defaultColorAttachment = defaultAttachments.first();
                Attachment defaultDepthAttachment = defaultAttachments.second();

                RenderPass renderPass1 = engine.createRenderPass(
                        "FIRST_renderToTexture",
                        0,
                        List.of(rttTargetAttachment),
                        List.of(new Color(1.0f, 1.0f, 1.0f, 1.0f)),
                        Option.none()
                );
                RenderPipelineBind pipelineBind1 = renderPass1.createPipelineBind(0, pipeline1);
                RenderTaskGroup taskGroup1 = pipelineBind1.createRenderTaskGroup(List.of());
                RenderTask task1 = taskGroup1.addRenderTask(
                        engine.createObject(new ObjectCreateInfo(
                                vertexInputInfo1,
                                MemorySegment.ofArray(VERTICES_OBJ1),
                                MemorySegment.ofArray(INDICES_OBJ1)
                        )),
                        List.of()
                );

                RenderPass renderPass2 = engine.createRenderPass(
                        "FINAL_presentToScreen",
                        1,
                        List.of(defaultColorAttachment),
                        List.of(new Color(0.0f, 0.0f, 0.2f, 1.0f)),
                        Option.some(defaultDepthAttachment)
                );
                renderPass2.addInputAttachments(List.of(rttTargetAttachment));
                RenderPipelineBind pipelineBind2 = renderPass2.createPipelineBind(0, pipeline2);
                RenderTaskGroup taskGroup2 = pipelineBind2.createRenderTaskGroup(List.of());
                RenderTask task2 = taskGroup2.addRenderTask(
                        engine.createObject(new ObjectCreateInfo(
                                vertexInputInfo2,
                                MemorySegment.ofArray(VERTICES_OBJ2),
                                MemorySegment.ofArray(INDICES_OBJ2)
                        )),
                        List.of(descriptorSet)
                );

                int counter = 0;
                while (true) {
                    Thread.sleep(1000 / 60);

                    float cosine = (float) Math.cos(counter * 0.01);
                    float sine = (float) Math.sin(counter * 0.01);
                    // rotate against the z-axis
                    ubo.updateBufferContent(MemorySegment.ofArray(new float[] {
                            // model matrix
                            cosine, -sine, 0.0f, 0.0f,
                            sine, cosine, 0.0f, 0.0f,
                            0.0f, 0.0f, 1.0f, 0.0f,
                            0.0f, 0.0f, 0.0f, 1.0f,

                            // identity for view matrix
                            1.0f, 0.0f, 0.0f, 0.0f,
                            0.0f, 1.0f, 0.0f, 0.0f,
                            0.0f, 0.0f, 1.0f, 0.0f,
                            0.0f, 0.0f, 0.0f, 1.0f,

                            // projection matrix
                            1.0f, 0.0f, 0.0f, 0.0f,
                            0.0f, 1.0f, 0.0f, 0.0f,
                            0.0f, 0.0f, 1.0f, 0.0f,
                            0.0f, 0.0f, 0.0f, 1.0f
                    }));
                    counter += 1;
                }
            } catch (RenderException | IOException | InterruptedException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }).start();
    }

    private static final float[] VERTICES_OBJ1 = {
            // vec2 position, vec3 color
            -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
            0.0f,  0.5f,  0.0f, 0.0f, 1.0f
    };

    private static final int[] INDICES_OBJ1 = {
            0, 1, 2
    };

    private static final float[] VERTICES_OBJ2 = {
            // vec3 position,   vec2 texCoord
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f,  0.5f, 1.0f, 0.0f,
            0.5f, 0.5f,   0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f,  0.5f, 0.0f, 1.0f
    };

    private static final int[] INDICES_OBJ2 = {
            0, 1, 2,
            2, 3, 0
    };

    private static final Logger logger = Logger.getLogger(DrillCreateObject.class.getName());
}

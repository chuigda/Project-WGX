package chr.wgx.drill;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ShaderStage;
import chr.wgx.render.common.UniformUpdateFrequency;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskGroup;
import org.jetbrains.annotations.Nullable;
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
                VertexInputInfo vii = new VertexInputInfo(List.of(
                        new FieldInfoInput("position", CGType.Vec3),
                        new FieldInfoInput("color", CGType.Vec3)
                ));

                ShaderProgram.Vulkan shaderProgram1 = new ShaderProgram.Vulkan(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.vert.spv"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.frag.spv")
                );

                UniformBufferBindingInfo ubbi = new UniformBufferBindingInfo(
                        List.of(new FieldInfoInput("color", CGType.Vec3)),
                        1,
                        ShaderStage.VERTEX
                );
                DescriptorSetLayoutCreateInfo dslci = new DescriptorSetLayoutCreateInfo(List.of(ubbi));
                DescriptorSetLayout layout = engine.createDescriptorSetLayout(dslci, 8);
                UniformBuffer ub = engine.createUniform(new UniformBufferCreateInfo(UniformUpdateFrequency.PER_FRAME, ubbi));
                ub.updateBufferContent(MemorySegment.ofArray(new float[]{1.0f, 0.0f, 0.0f}));
                DescriptorSet descriptorSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(layout, List.of(ub)));

                RenderPipelineCreateInfo rpci = new RenderPipelineCreateInfo(
                        vii,
                        List.of(layout),
                        List.of(),
                        Option.some(shaderProgram1),
                        Option.none(),
                        1,
                        true
                );

                logger.info("运行测试项目: 创建渲染管线");
                RenderPipeline pipeline = engine.createPipeline(rpci);
                Pair<Attachment, Attachment> defaultAttachments = engine.getDefaultAttachments();

                RenderPass renderPass = engine.createRenderPass(
                        "FINAL_outputToSwapchain",
                        5000,
                        List.of(defaultAttachments.first()),
                        Option.some(defaultAttachments.second())
                );
                RenderPipelineBind pipelineBind = renderPass.createPipelineBind(0, pipeline);
                RenderTaskGroup taskGroup = pipelineBind.createRenderTaskGroup(List.of());

                logger.info("运行测试项目: 创建对象");
                RenderObject object1 = engine.createObject(new ObjectCreateInfo(
                        vii,
                        MemorySegment.ofArray(VERTICES_OBJ1),
                        MemorySegment.ofArray(INDICES_OBJ1)
                ));
                logger.info("对象已创建: " + object1);

                logger.info("运行测试项目: 添加渲染任务");
                RenderTask _task = taskGroup.addRenderTask(object1, List.of(descriptorSet));
                logger.info("渲染任务已添加");

                int counter = 0;
                while (true) {
                    try {
                        Thread.sleep(16);
                        ub.updateBufferContent(MemorySegment.ofArray(new float[]{
                                (float) Math.sin(counter / 90.0),
                                (float) Math.cos(counter / 90.0),
                                1.0f
                        }));
                        counter += 1;
                        if (counter >= 360) {
                            counter = 0;
                        }
                    } catch (InterruptedException e) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                }
            } catch (RenderException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }).start();
    }

    private static byte[] DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader(String path) {
        try (@Nullable InputStream stream = DrillCreateObject.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new RuntimeException("找不到文件: " + path);
            }

            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final float[] VERTICES_OBJ1 = {
            // vec3 position,   vec3 color
            -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f,  0.5f, 1.0f, 1.0f, 0.0f,
            0.5f, 0.5f,   0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f, 0.5f,  0.5f, 0.0f, 0.0f, 1.0f
    };

    private static final int[] INDICES_OBJ1 = {
            0, 1, 2,
            2, 3, 0
    };

    private static final Logger logger = Logger.getLogger(DrillCreateObject.class.getName());
}

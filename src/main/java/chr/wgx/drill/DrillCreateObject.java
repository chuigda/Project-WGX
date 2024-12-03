package chr.wgx.drill;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
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
                VertexInputInfo vii1 = new VertexInputInfo(List.of(
                        new VertexInputInfo.AttributeIn("position", CGType.Vec3),
                        new VertexInputInfo.AttributeIn("color", CGType.Vec3)
                ));

                logger.info("运行测试项目: 在线程中创建对象");
                MemorySegment s1 = MemorySegment.ofArray(VERTICES_OBJ1);
                ObjectHandle handle1 = engine.createObject(new ObjectCreateInfo(vii1, s1));
                logger.info("对象已创建: " + handle1);

                VertexInputInfo vii2 = new VertexInputInfo(List.of(
                        new VertexInputInfo.AttributeIn("position", CGType.Vec3)
                ));

                logger.info("运行测试项目: 在线程中创建第二个对象");
                MemorySegment s2 = MemorySegment.ofArray(VERTICES_OBJ2);
                ObjectHandle handle2 = engine.createObject(new ObjectCreateInfo(vii2, s2));
                logger.info("第二个对象已创建: " + handle2);

                logger.info("运行测试项目: 创建渲染管线");
                ShaderProgram.Vulkan shaderProgram1 = new ShaderProgram.Vulkan(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.vert.spv"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.frag.spv")
                );
                ShaderProgram.GLES2 shaderProgram1GLES = new ShaderProgram.GLES2(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShaderText("/resources/shader/gles2/drill.vert"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShaderText("/resources/shader/gles2/drill.frag")
                );

                RenderPipelineCreateInfo rpci = new RenderPipelineCreateInfo(
                        vii1,
                        List.of(),
                        List.of(),
                        Option.some(shaderProgram1),
                        Option.some(shaderProgram1GLES),
                        1,
                        true
                );
                RenderPipelineHandle pipelineHandle = engine.createPipeline(rpci);
                logger.info("渲染管线已创建: " + pipelineHandle);

                logger.info("运行测试项目: 创建第二个渲染管线");
                ShaderProgram.Vulkan shaderProgram2 = new ShaderProgram.Vulkan(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill2.vert.spv"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill2.frag.spv")
                );
                ShaderProgram.GLES2 shaderProgram2GLES = new ShaderProgram.GLES2(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShaderText("/resources/shader/gles2/drill2.vert"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShaderText("/resources/shader/gles2/drill2.frag")
                );
                RenderPipelineCreateInfo rpci2 = new RenderPipelineCreateInfo(
                        vii2,
                        List.of(),
                        List.of(),
                        Option.some(shaderProgram2),
                        Option.some(shaderProgram2GLES),
                        1,
                        true
                );
                RenderPipelineHandle pipelineHandle2 = engine.createPipeline(rpci2);
                logger.info("第二个渲染管线已创建: " + pipelineHandle2);

//                Pair<ColorAttachmentHandle, DepthAttachmentHandle> defaultAttachments = engine.getDefaultAttachments();
//                ColorAttachmentHandle defaultColorAttachment = defaultAttachments.first();

                logger.info("运行测试项目: 创建渲染任务");
                RenderTaskInfo rti = new RenderTaskInfo(
                        RenderTaskInfo.PRIORITY_NOT_IMPORTANT,
                        pipelineHandle,
                        List.of(handle1),
                        List.of(),
                        Option.none()
                );
                RenderTaskHandle renderTaskHandle = engine.createTask(rti);
                logger.info("渲染任务已创建: " + renderTaskHandle);
                logger.info("运行测试项目: 创建第二个渲染任务");
                RenderTaskInfo rti2 = new RenderTaskInfo(
                        RenderTaskInfo.PRIORITY_NOT_IMPORTANT,
                        pipelineHandle2,
                        List.of(handle2),
                        List.of(),
                        Option.none()
                );
                RenderTaskHandle renderTaskHandle2 = engine.createTask(rti2);
                logger.info("第二个渲染任务已创建: " + renderTaskHandle2);
            } catch (RenderException e) {
                logger.severe("创建对象失败: " + e.getMessage());
            }
        }).start();
    }

    private static byte[] DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader(String path) {
        try (InputStream stream  = DrillCreateObject.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new RuntimeException("找不到文件: " + path);
            }

            return stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShaderText(String path) {
        try (InputStream stream  = DrillCreateObject.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new RuntimeException("找不到文件: " + path);
            }

            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final float[] VERTICES_OBJ1 = {
            // vec3 position, vec3 color
            -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.5f,   0.5f, 0.0f, 0.0f, 1.0f,
    };

    private static final float[] VERTICES_OBJ2 = {
            // vec3 position
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f,  1.0f,
            0.0f, 0.5f,   0.5f,
    };

    private static final Logger logger = Logger.getLogger(DrillCreateObject.class.getName());
}

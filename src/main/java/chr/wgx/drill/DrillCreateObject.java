package chr.wgx.drill;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.handle.RenderPipelineHandle;
import chr.wgx.render.handle.RenderTaskHandle;
import chr.wgx.render.info.*;
import tech.icey.panama.buffer.FloatBuffer;
import tech.icey.xjbutil.container.Option;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.util.List;
import java.util.logging.Logger;

public class DrillCreateObject {
    public static void createObjectInThread(AbstractRenderEngine engine) {
        new Thread(() -> {
            try (Arena arena = Arena.ofConfined()) {
                VertexInputInfo vii = new VertexInputInfo(List.of(
                        new VertexInputInfo.AttributeIn("position", CGType.Vec2),
                        new VertexInputInfo.AttributeIn("color", CGType.Vec3)
                ));

                logger.info("运行测试项目: 在线程中创建对象");
                FloatBuffer buffer = FloatBuffer.allocate(arena, VERTICES.length);
                for (int i = 0; i < VERTICES.length; i++) {
                    buffer.write(i, VERTICES[i]);
                }

                ObjectCreateInfo oci = new ObjectCreateInfo(vii, buffer);
                ObjectHandle handle = engine.createObject(oci);
                logger.info("对象已创建: " + handle);

                logger.info("运行测试项目: 创建渲染管线");
                ShaderProgram.Vulkan shaderProgram = new ShaderProgram.Vulkan(
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.vert.spv"),
                        DRILL_FUNCTION_DO_NOT_USE_IN_PRODUCT_OR_YOU_WILL_BE_FIRED_readShader("/resources/shader/vk/drill.frag.spv")
                );

                RenderPipelineCreateInfo rpci = new RenderPipelineCreateInfo(
                        vii,
                        List.of(),
                        List.of(),
                        Option.some(shaderProgram),
                        Option.none(),
                        1,
                        false
                );
                RenderPipelineHandle pipelineHandle = engine.createPipeline(rpci);
                logger.info("渲染管线已创建: " + pipelineHandle);

                RenderTaskInfo rti = new RenderTaskInfo(pipelineHandle, List.of(handle));
                RenderTaskHandle renderTaskHandle = engine.createTask(rti);
                logger.info("渲染任务已创建: " + renderTaskHandle);
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

    private static final float[] VERTICES = {
            // vec2 position, vec3 color
            -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f,  0.0f, 1.0f, 0.0f,
            0.0f, 0.5f,   0.0f, 0.0f, 1.0f,
    };

    private static final Logger logger = Logger.getLogger(DrillCreateObject.class.getName());
}

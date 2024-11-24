package chr.wgx.drill;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.CGType;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.info.ObjectCreateInfo;
import chr.wgx.render.info.VertexInputInfo;
import tech.icey.panama.buffer.FloatBuffer;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.logging.Logger;

public class DrillCreateObject {
    public static void createObjectInThread(AbstractRenderEngine engine) {
        new Thread(() -> {
            logger.info("运行测试项目: 在线程中创建对象");

            try (Arena arena = Arena.ofConfined()) {
                FloatBuffer buffer = FloatBuffer.allocate(arena, VERTICES.length);
                for (int i = 0; i < VERTICES.length; i++) {
                    buffer.write(i, VERTICES[i]);
                }

                VertexInputInfo vii = new VertexInputInfo(List.of(
                        new VertexInputInfo.AttributeIn("position", CGType.Vec3),
                        new VertexInputInfo.AttributeIn("color", CGType.Vec2)
                ));

                ObjectCreateInfo oci = new ObjectCreateInfo(vii, buffer);
                ObjectHandle handle = engine.createObject(oci);
                logger.info("对象已创建: " + handle);
            } catch (RenderException e) {
                logger.severe("创建对象失败: " + e.getMessage());
            }
        }).start();
    }

    private static final float[] VERTICES = {
            // vec2 position, vec3 color
            -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
    };

    private static final Logger logger = Logger.getLogger(DrillCreateObject.class.getName());
}

package chr.wgx.builtin.wgcv1;

import chr.wgx.reactor.IWidget;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginBehavior;
import chr.wgx.reactor.plugin.IWidgetProvider;
import chr.wgx.render.RenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.*;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.util.ResourceUtil;
import org.joml.Matrix4f;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.util.List;

public final class WGCV1 implements IPlugin, IWidgetProvider {
    WGCV1(Reactor reactor) throws IOException, RenderException {
        RenderEngine engine = reactor.renderEngine;

        UniformBufferBindingInfo viewProjBindingInfo = new UniformBufferBindingInfo(
                "uVP",
                ShaderStage.VERTEX,
                List.of(
                        new FieldInfoInput("view", CGType.Mat4),
                        new FieldInfoInput("projection", CGType.Mat4)
                )
        );
        UniformBufferBindingInfo materialBindingInfo = new UniformBufferBindingInfo(
                "uMaterial",
                ShaderStage.FRAGMENT,
                List.of(
                        new FieldInfoInput("ambient", CGType.Vec4),
                        new FieldInfoInput("diffuse", CGType.Vec4)
                )
        );
        DescriptorSetLayout colorPassDescriptorSetLayout = engine.createDescriptorSetLayout(
                new DescriptorSetLayoutCreateInfo(List.of(viewProjBindingInfo, materialBindingInfo)),
                32
        );
        reactor.stablePool.put("WGCV1_ViewProjSetLayout", colorPassDescriptorSetLayout);

        this.viewProjBuffer = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );

        float[] viewProjData = new float[32];
        Matrix4f view = new Matrix4f();
        view.scale(1.0f, -1.0f, 1.0f);
        view.lookAt(40.0f, 40.0f, 40.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        view.get(viewProjData, 0);
        Matrix4f projection = new Matrix4f();
        projection.perspective(
                (float) Math.toRadians(45.0f),
                reactor.framebufferWidth / (float) reactor.framebufferHeight,
                0.1f,
                100.0f,
                true
        );
        projection.get(viewProjData, 16);

        viewProjBuffer.updateBufferContent(MemorySegment.ofArray(viewProjData));
        reactor.stablePool.put("WGCV1_ViewProj", viewProjBuffer);

        UniformBuffer plasticMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, materialBindingInfo)
        );
        UniformBuffer chromeMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, materialBindingInfo)
        );
        UniformBuffer steelMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, materialBindingInfo)
        );
        UniformBuffer brassMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, materialBindingInfo)
        );
        plasticMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.2f, 0.2f, 0.2f, 1.0f,
                0.55f, 0.55f, 0.55f, 1.0f
        }));
        chromeMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.25f, 0.25f, 0.25f, 1.0f,
                0.4f, 0.4f, 0.4f, 1.0f
        }));
        steelMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.15f, 0.15f, 0.15f, 1.0f,
                0.25f, 0.25f, 0.25f, 1.0f
        }));
        brassMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.329412f, 0.223529f, 0.027451f, 1.0f,
                0.780392f, 0.568627f, 0.113725f, 1.0f
        }));
        reactor.stablePool.put("WGCV1_PlasticMaterial", plasticMaterial);
        reactor.stablePool.put("WGCV1_ChromeMaterial", chromeMaterial);
        reactor.stablePool.put("WGCV1_SteelMaterial", steelMaterial);
        reactor.stablePool.put("WGCV1_BrassMaterial", brassMaterial);

        PushConstantRange pushConstantRange = new PushConstantRange("model", ShaderStage.VERTEX, CGType.Mat4, 0);
        PushConstantInfo pushConstantInfo = new PushConstantInfo(List.of(pushConstantRange));

        VertexInputInfo colorPassVertexInputInfo = new VertexInputInfo(List.of(
                new FieldInfoInput("position", CGType.Vec3),
                new FieldInfoInput("normal", CGType.Vec3)
        ));
        reactor.stablePool.put("WGCV1_MajorPassVertexInputInfo", colorPassVertexInputInfo);

        Pair<float[], int[]> chestBoxData = MeshReader.parseV1Mesh(ResourceUtil.readTextFile(
                "/resources/model/wgc0310v1/chest-box.mesh"
        ));
        RenderObject chest = engine.createObject(new ObjectCreateInfo(
                colorPassVertexInputInfo,
                MemorySegment.ofArray(chestBoxData.first()),
                MemorySegment.ofArray(chestBoxData.second())
        ));
        PushConstant pcChestModel = engine.createPushConstant(pushConstantInfo, 1).getFirst();
        pcChestModel.updateBufferContent(MemorySegment.ofArray(new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        }));

        DescriptorSet plasticMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, plasticMaterial)
        ));

        Pair<Attachment, Attachment> defaultAttachments = engine.getDefaultAttachments();

        RenderPipeline colorPassPipeline = engine.createPipeline(new RenderPipelineCreateInfo(
                colorPassVertexInputInfo,
                List.of(colorPassDescriptorSetLayout),
                Option.some(pushConstantInfo),
                Option.some(new ShaderProgram.Vulkan(
                        ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/phong.vert.spv"),
                        ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/phong.frag.spv")
                )),
                Option.none(),
                1,
                true
        ));

        RenderPass colorPass = engine.createRenderPass(new RenderPassCreateInfo(
                "WGCV1_FINAL_colorPass",
                2000,
                new RenderPassAttachmentInfo(
                        defaultAttachments.first(),
                        ClearBehavior.CLEAR_ONCE,
                        new Color(0.0f, 0.2f, 0.2f, 1.0f)
                ),
                new RenderPassAttachmentInfo(
                        defaultAttachments.second(),
                        ClearBehavior.CLEAR_ONCE
                )
        ));

        RenderPipelineBind bind = colorPass.createPipelineBind(2000, colorPassPipeline);
        RenderTaskGroup plasticGroup = bind.createRenderTaskGroup(List.of(plasticMaterialSet));
        RenderTask chestBoxTask = plasticGroup.addRenderTask(chest, List.of(), pcChestModel);
    }

    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of(
                new IPluginBehavior() {
                    @Override
                    public String name() {
                        return "WGCV1_UpdateViewProj";
                    }

                    @Override
                    public String description() {
                        return "用于更新视图投影矩阵";
                    }

                    @Override
                    public int priority() {
                        return 0;
                    }

                    @Override
                    public void tick(Reactor reactor) throws Exception {
                        if (reactor.framebufferResized) {
                            float[] viewProjData = new float[32];
                            Matrix4f view = new Matrix4f();
                            view.scale(1.0f, -1.0f, 1.0f);
                            view.lookAt(40.0f, 40.0f, 40.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
                            view.get(viewProjData, 0);
                            Matrix4f projection = new Matrix4f();
                            projection.perspective(
                                    (float) Math.toRadians(45.0f),
                                    reactor.framebufferWidth / (float) reactor.framebufferHeight,
                                    0.1f,
                                    100.0f,
                                    true
                            );
                            projection.get(viewProjData, 16);

                            viewProjBuffer.updateBufferContent(MemorySegment.ofArray(viewProjData));
                        }
                    }
                }
        );
    }

    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of();
    }

    private final UniformBuffer viewProjBuffer;
}

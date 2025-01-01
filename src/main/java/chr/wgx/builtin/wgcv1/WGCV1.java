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
                        new FieldInfoInput("diffuse", CGType.Vec4),
                        new FieldInfoInput("specular", CGType.Vec4),
                        new FieldInfoInput("shininess", CGType.Float)
                )
        );
        DescriptorSetLayout colorPassDescriptorSetLayout = engine.createDescriptorSetLayout(
                new DescriptorSetLayoutCreateInfo(List.of(viewProjBindingInfo, materialBindingInfo)),
                32
        );
        reactor.stablePool.put("WGCV1_ViewProjSetLayout", colorPassDescriptorSetLayout);

        UniformBuffer viewProj = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );
        viewProj.updateBufferContent(segment -> {
            Matrix4f view = new Matrix4f();
            view.lookAt(0.0f, 0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            view.get(segment.asByteBuffer().asFloatBuffer());

            Matrix4f projection = new Matrix4f();
            projection.scale(1.0f, -1.0f, 1.0f)
                    .perspective((float) Math.toRadians(45.0f), 1.0f, 0.1f, 100.0f, true);
            projection.get(segment.asSlice(16 * Float.BYTES).asByteBuffer().asFloatBuffer());
        });
        reactor.stablePool.put("WGCV1_ViewProj", viewProj);

        UniformBuffer plasticMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );
        UniformBuffer chromeMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );
        UniformBuffer steelMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );
        UniformBuffer brassMaterial = engine.createUniform(
                new UniformBufferCreateInfo(UniformUpdateFrequency.MANUAL, viewProjBindingInfo)
        );
        plasticMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.2f, 0.2f, 0.2f, 1.0f,
                0.55f, 0.55f, 0.55f, 1.0f,
                0.7f, 0.7f, 0.7f, 1.0f,
                32.0f
        }));
        chromeMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.25f, 0.25f, 0.25f, 1.0f,
                0.4f, 0.4f, 0.4f, 1.0f,
                0.774597f, 0.774597f, 0.774597f, 1.0f,
                76.8f
        }));
        steelMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.15f, 0.15f, 0.15f, 1.0f,
                0.25f, 0.25f, 0.25f, 1.0f,
                0.774597f, 0.774597f, 0.774597f, 1.0f,
                76.8f
        }));
        brassMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.329412f, 0.223529f, 0.027451f, 1.0f,
                0.780392f, 0.568627f, 0.113725f, 1.0f,
                0.992157f, 0.941176f, 0.807843f, 1.0f,
                27.8974f
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
                0.01f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.01f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.01f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        }));

        DescriptorSet plasticMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProj, plasticMaterial)
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
        return List.of();
    }

    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of();
    }
}

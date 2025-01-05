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
                        new FieldInfoInput("proj", CGType.Mat4)
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
        fillViewProj(viewProjBuffer, reactor.framebufferWidth, reactor.framebufferHeight);
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
        UniformBuffer blackPlasticMaterial = engine.createUniform(
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
        blackPlasticMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                0.1f, 0.1f, 0.1f, 1.0f,
                0.25f, 0.25f, 0.25f, 1.0f
        }));
        reactor.stablePool.put("WGCV1_PlasticMaterial", plasticMaterial);
        reactor.stablePool.put("WGCV1_ChromeMaterial", chromeMaterial);
        reactor.stablePool.put("WGCV1_SteelMaterial", steelMaterial);
        reactor.stablePool.put("WGCV1_BrassMaterial", brassMaterial);
        reactor.stablePool.put("WGCV1_BlackPlasticMaterial", blackPlasticMaterial);

        DescriptorSet plasticMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, plasticMaterial)
        ));
        DescriptorSet chromeMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, chromeMaterial)
        ));
        DescriptorSet steelMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, steelMaterial)
        ));
        DescriptorSet brassMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, brassMaterial)
        ));
        DescriptorSet blackPlasticMaterialSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(viewProjBuffer, blackPlasticMaterial)
        ));

        PushConstantRange pushConstantRange = new PushConstantRange("model", ShaderStage.VERTEX, CGType.Mat4, 0);
        PushConstantInfo pushConstantInfo = new PushConstantInfo(List.of(pushConstantRange));
//
        VertexInputInfo colorPassVertexInfo = new VertexInputInfo(List.of(
                new FieldInfoInput("position", CGType.Vec3),
                new FieldInfoInput("normal", CGType.Vec3)
        ));
        reactor.stablePool.put("WGCV1_MajorPassVertexInputInfo", colorPassVertexInfo);

        RenderObject chestBox = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/chest-box.mesh");
        RenderObject chestPlate = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/chest-plate.mesh");
        RenderObject powerSlot = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/power.mesh");
        RenderObject powerPin = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/power-pin.mesh");
        RenderObject headWheel = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/wheel.mesh");
        RenderObject monitor = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/monitor.mesh");
        RenderObject monitorIntake = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/monitor-intake.mesh");
        RenderObject waist = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/waist.mesh");
        RenderObject abdomen = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/abdomen.mesh");
        RenderObject shoulderConnector = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/shoulder-connector.mesh");
        RenderObject shoulderPlate = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/shoulder-plate.mesh");
        RenderObject bigArm = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/big-arm.mesh");
        RenderObject bigArmCover = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/big-arm-cover.mesh");
        RenderObject bigArmConnector = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/big-arm-connector.mesh");
        RenderObject wheelSmall = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/wheel-small.mesh");
        RenderObject smallArm = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/small-arm.mesh");
        RenderObject smallArmCover = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/small-arm-cover.mesh");
        RenderObject claw = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/claw.mesh");
        RenderObject clawCover = loadMeshFromResc(engine, colorPassVertexInfo, "/resources/model/wgc0310v1/claw-cover.mesh");

        List<PushConstant> pushConstants = engine.createPushConstant(pushConstantInfo, 20 + 9);
        this.pcAbdomen = new PushConstant[10];
        this.pcWaist = new PushConstant[10];
        for (int i = 0; i < 10; i++) {
            this.pcAbdomen[i] = pushConstants.get(i);
            this.pcWaist[i] = pushConstants.get(i + 10);
        }

        this.pcChest = pushConstants.get(20);
        this.pcHeadWheel = pushConstants.get(21);
        this.pcHead = pushConstants.get(22);

        this.pcLeftShoulder = pushConstants.get(23);
        this.pcLeftWheel = pushConstants.get(24);
        this.pcLeftBigArm = pushConstants.get(25);
        this.pcLeftBigArmConnector = pushConstants.get(26);
        this.pcLeftSmallArm = pushConstants.get(27);
        this.pcLeftClaw = pushConstants.get(28);

        Matrix4f transform0 = new Matrix4f().identity();
        transform0.translate(0.0f, -12.0f, 0.0f);

        for (int i = 0; i < 10; i++) {
            fillPushConstant(this.pcWaist[i], transform0);
            transform0.translate(0.0f, 1.0f, 0.0f);
            fillPushConstant(this.pcAbdomen[i], transform0);
            transform0.translate(0.0f, 1.0f, 0.0f);
        }
        transform0.translate(0.0f, 1.0f, 0.0f);
        fillPushConstant(this.pcChest, transform0);

        Matrix4f transform1 = new Matrix4f(transform0);
        transform1.translate(0.0f, 12.875f, 0.0f);
        fillPushConstant(this.pcHeadWheel, transform1);
        transform1.translate(0.0f, 0.375f, 0.0f);
        fillPushConstant(this.pcHead, transform1);

        Matrix4f transform2 = new Matrix4f(transform0);
        transform2.translate(14.5f, 7.5f, 0.0f);
        fillPushConstant(this.pcLeftShoulder, transform2);
        transform2.translate(4.75f, 0.0f, 0.0f);
        transform2.rotateZ((float) Math.toRadians(-90.0f));
        fillPushConstant(this.pcLeftBigArm, transform2);
        transform2.translate(20.0f, 0.0f, 0.0f);
        fillPushConstant(this.pcLeftBigArmConnector, transform2);
        transform2.translate(4.5f, 0.0f, 0.0f);
        fillPushConstant(this.pcLeftSmallArm, transform2);
        transform2.translate(25.0f, 0.0f, 0.0f);
        fillPushConstant(this.pcLeftClaw, transform2);

        Pair<Attachment, Attachment> defaultAttachments = engine.getDefaultAttachments();
//
        RenderPipeline colorPassPipeline = engine.createPipeline(new RenderPipelineCreateInfo(
                colorPassVertexInfo,
                List.of(colorPassDescriptorSetLayout),
                Option.some(pushConstantInfo),
                Option.some(new ShaderProgram.Vulkan(
                        ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/phong.vert.spv"),
                        ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/phong.frag.spv")
                )),
                Option.some(new ShaderProgram.GLES2(
                        ResourceUtil.readTextFile("/resources/shader/wgc0310v1/gles2/phong.vert"),
                        ResourceUtil.readTextFile("/resources/shader/wgc0310v1/gles2/phong.frag")
                )),
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
        RenderTask chestBoxTask = plasticGroup.addRenderTask(chestBox, pcChest);
        RenderTask headTask = plasticGroup.addRenderTask(monitor, pcHead);
        for (int i = 0; i < 10; i++) {
            RenderTask waistTask = plasticGroup.addRenderTask(waist, pcWaist[i]);
        }
        RenderTask leftShoulderConnectorTask = plasticGroup.addRenderTask(shoulderConnector, pcLeftShoulder);
        RenderTask leftBigArmCoverTask = plasticGroup.addRenderTask(bigArmCover, pcLeftBigArm);
        RenderTask leftSmallArmCoverTask = plasticGroup.addRenderTask(smallArmCover, pcLeftSmallArm);
        RenderTask leftClawCoverTask = plasticGroup.addRenderTask(clawCover, pcLeftClaw);

        RenderTaskGroup chromeGroup = bind.createRenderTaskGroup(List.of(chromeMaterialSet));
        RenderTask chestPlateTask = chromeGroup.addRenderTask(chestPlate, pcChest);
        RenderTask monitorIntakeTask = chromeGroup.addRenderTask(monitorIntake, pcHead);
        RenderTask leftShoulderPlateTask = chromeGroup.addRenderTask(shoulderPlate, pcLeftShoulder);
        RenderTask leftBigArmTask = chromeGroup.addRenderTask(bigArm, pcLeftBigArm);
        RenderTask leftSmallArmtask = chromeGroup.addRenderTask(smallArm, pcLeftSmallArm);

        RenderTaskGroup steelGroup = bind.createRenderTaskGroup(List.of(steelMaterialSet));
        RenderTask leftBigArmConnectorTask = steelGroup.addRenderTask(bigArmConnector, pcLeftBigArmConnector);
        RenderTask leftClawTask = steelGroup.addRenderTask(claw, pcLeftClaw);

        RenderTaskGroup brassGroup = bind.createRenderTaskGroup(List.of(brassMaterialSet));
        RenderTask powerPinTask = brassGroup.addRenderTask(powerPin, pcChest);

        RenderTaskGroup blackPlasticGroup = bind.createRenderTaskGroup(List.of(blackPlasticMaterialSet));
        RenderTask powerSlotTask = blackPlasticGroup.addRenderTask(powerSlot, pcChest);
        RenderTask headWheelTask = blackPlasticGroup.addRenderTask(headWheel, pcHeadWheel);
        for (int i = 0; i < 10; i++) {
            RenderTask abdomenTask = blackPlasticGroup.addRenderTask(abdomen, pcAbdomen[i]);
        }
        RenderTask leftShoulderWheel = blackPlasticGroup.addRenderTask(wheelSmall, pcLeftWheel);
        RenderTask leftSmallWheel = blackPlasticGroup.addRenderTask(wheelSmall, pcLeftSmallArm);

        RenderObject testObject = engine.createObject(new ObjectCreateInfo(
                colorPassVertexInfo,
                MemorySegment.ofArray(new float[]{
                        // a rect, with normal pointing out
                        // vec3 position, vec3 normal
                        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f,
                        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f
                }),
                MemorySegment.ofArray(new int[]{
                        0, 1, 2,
                        0, 2, 3
                })
        ));
        UniformBuffer testObjectViewProjBuffer = engine.createUniform(new UniformBufferCreateInfo(
                UniformUpdateFrequency.MANUAL,
                viewProjBindingInfo
        ));
        UniformBuffer testMaterial = engine.createUniform(new UniformBufferCreateInfo(
                UniformUpdateFrequency.MANUAL,
                materialBindingInfo
        ));
        DescriptorSet testObjectSet = engine.createDescriptorSet(new DescriptorSetCreateInfo(
                colorPassDescriptorSetLayout,
                List.of(testObjectViewProjBuffer, testMaterial)
        ));
        PushConstant pcTestObject = engine.createPushConstant(pushConstantInfo, 1).getFirst();

        testObjectViewProjBuffer.updateBufferContent(MemorySegment.ofArray(new float[]{
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,

                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        }));
        testMaterial.updateBufferContent(MemorySegment.ofArray(new float[]{
                1.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        }));
        pcTestObject.updateBufferContent(MemorySegment.ofArray(new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        }));

        RenderTaskGroup testObjectGroup = bind.createRenderTaskGroup(List.of(testObjectSet));
        RenderTask testObjectTask = testObjectGroup.addRenderTask(testObject, pcTestObject);
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
                    public void tick(Reactor reactor) {
                        if (reactor.framebufferResized) {
                            fillViewProj(viewProjBuffer, reactor.framebufferWidth, reactor.framebufferHeight);
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
    private final PushConstant pcAbdomen[];
    private final PushConstant pcWaist[];
    private final PushConstant pcChest;
    private final PushConstant pcHeadWheel;
    private final PushConstant pcHead;
    private final PushConstant pcLeftShoulder;
    private final PushConstant pcLeftBigArm;
    private final PushConstant pcLeftWheel;
    private final PushConstant pcLeftBigArmConnector;
    private final PushConstant pcLeftSmallArm;
    private final PushConstant pcLeftClaw;

    private static RenderObject loadMeshFromResc(
            RenderEngine engine,
            VertexInputInfo vertexInputInfo,
            String path
    ) throws IOException, RenderException {
        Pair<float[], int[]> data = MeshReader.parseV1Mesh(ResourceUtil.readTextFile(path));
        return engine.createObject(new ObjectCreateInfo(
                vertexInputInfo,
                MemorySegment.ofArray(data.first()),
                MemorySegment.ofArray(data.second())
        ));
    }

    private static void fillViewProj(UniformBuffer viewProjBuffer, int width, int height) {
        float[] viewProjData = new float[32];
        Matrix4f view = new Matrix4f();
        view.scale(1.0f, -1.0f, 1.0f);
        view.lookAt(20.0f, 20.0f, 120.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        view.get(viewProjData, 0);
        Matrix4f projection = new Matrix4f();
        projection.perspective(
                (float) Math.toRadians(45.0f),
                width / (float) height,
                0.1f,
                1000.0f,
                true
        );
        projection.get(viewProjData, 16);

        viewProjBuffer.updateBufferContent(MemorySegment.ofArray(viewProjData));
    }

    private static void fillPushConstant(PushConstant pc, Matrix4f model) {
        float[] modelData = new float[16];
        model.get(modelData);
        pc.updateBufferContent(MemorySegment.ofArray(modelData));
    }
}

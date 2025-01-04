package chr.wgx.drill;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginBehavior;
import chr.wgx.reactor.plugin.IPluginFactory;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.List;

public final class DrillPlugin implements IPlugin {
    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of();
    }

    public static class Factory implements IPluginFactory {
        @Override
        public String name() {
            return "JavaScript 测试插件";
        }

        @Override
        public String description() {
            return "用于测试 JavaScript 脚本的插件";
        }

        @Override
        public int initPriority() {
            return 1000;
        }

        @Override
        public IPlugin create(Reactor reactor) throws Exception {
            NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
            ScriptEngine engine = factory.getScriptEngine("--language=es6");

            engine.put("engine", reactor.renderEngine);
            engine.eval("""
"use strict";
(function() {
  // src/packages/chr.wgx.render.ts
  var BlendMode = Java.type("chr.wgx.render.common.BlendMode");
  var CGType = Java.type("chr.wgx.render.common.CGType");
  var ClearBehavior = Java.type("chr.wgx.render.common.ClearBehavior");
  var PixelFormat = Java.type("chr.wgx.render.common.PixelFormat");
  var ShaderStage = Java.type("chr.wgx.render.common.ShaderStage");
  var UniformUpdateFrequency = Java.type("chr.wgx.render.common.UniformUpdateFrequency");
  var Color = Java.type("chr.wgx.render.common.Color");
  var DescriptorType = Java.type("chr.wgx.render.info.DescriptorType");
  var AttachmentCreateInfo = Java.type("chr.wgx.render.info.AttachmentCreateInfo");
  var FieldInfoInput = Java.type("chr.wgx.render.info.FieldInfoInput");
  var UniformBufferBindingInfo = Java.type("chr.wgx.render.info.UniformBufferBindingInfo");
  var DescriptorSetCreateInfo = Java.type("chr.wgx.render.info.DescriptorSetCreateInfo");
  var DescriptorSetLayout = Java.type("chr.wgx.render.data.DescriptorSetLayout");
  var DescriptorSetLayoutCreateInfo = Java.type("chr.wgx.render.info.DescriptorSetLayoutCreateInfo");
  var ObjectCreateInfo = Java.type("chr.wgx.render.info.ObjectCreateInfo");
  var VertexInputInfo = Java.type("chr.wgx.render.info.VertexInputInfo");
  var PushConstantInfo = Java.type("chr.wgx.render.info.PushConstantInfo");
  var PushConstantRange = Java.type("chr.wgx.render.info.PushConstantRange");
  var RenderPassAttachmentInfo = Java.type("chr.wgx.render.info.RenderPassAttachmentInfo");
  var RenderPassCreateInfo = Java.type("chr.wgx.render.info.RenderPassCreateInfo");
  var RenderPipelineCreateInfo = Java.type("chr.wgx.render.info.RenderPipelineCreateInfo");
  var ShaderProgram = Java.type("chr.wgx.render.info.ShaderProgram");
  var TextureBindingInfo = Java.type("chr.wgx.render.info.TextureBindingInfo");
  var TextureCreateInfo = Java.type("chr.wgx.render.info.TextureCreateInfo");
  var UniformBufferCreateInfo = Java.type("chr.wgx.render.info.UniformBufferCreateInfo");

  // src/packages/chr.wgx.util.ts
  var ResourceUtil = Java.type("chr.wgx.util.ResourceUtil");

  // src/packages/java.ts
  var ByteArray = Java.type("byte[]");
  var IntArray = Java.type("int[]");
  var FloatArray = Java.type("float[]");
  function makeIntSegment(array) {
    var intArray = Java.to(array, IntArray);
    return MemorySegment.ofArray(intArray);
  }
  function makeFloatSegment(array) {
    var floatArray = Java.to(array, FloatArray);
    return MemorySegment.ofArray(floatArray);
  }
  var MemorySegment = java.lang.foreign.MemorySegment;
  var ArrayList = java.util.ArrayList;

  // src/packages/tech.icey.xjbutil.ts
  var Pair = Java.type("tech.icey.xjbutil.container.Pair");
  var Option = Java.type("tech.icey.xjbutil.container.Option");

  // src/entry/drill.ts
  var vertexInputInfo = new VertexInputInfo([
    new FieldInfoInput("position", CGType.Vec2),
    new FieldInfoInput("color", CGType.Vec3)
  ]);
  var object = engine.createObject(new ObjectCreateInfo(vertexInputInfo, makeFloatSegment([
    // vec2 position, vec3 color
    -0.25,
    -0.25,
    1,
    0,
    0,
    0.25,
    -0.25,
    1,
    1,
    0,
    0.25,
    0.25,
    0,
    1,
    0,
    -0.25,
    0.25,
    0,
    0,
    1
  ]), makeIntSegment([
    0,
    1,
    2,
    2,
    3,
    0
  ])));
  var pipeline = engine.createPipeline(new RenderPipelineCreateInfo(vertexInputInfo, [], Option.none(), Option.some(new ShaderProgram.Vulkan(ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/screen_content.vert.spv"), ResourceUtil.readBinaryFile("/resources/shader/wgc0310v1/vk/screen_content.frag.spv"))), Option.some(new ShaderProgram.GLES2(ResourceUtil.readTextFile("/resources/shader/wgc0310v1/gles2/screen_content.vert"), ResourceUtil.readTextFile("/resources/shader/wgc0310v1/gles2/screen_content.frag"))), 1, false));
  var defaultColorAttachment = engine.getDefaultAttachments().first();
  var renderPass = engine.createRenderPass(new RenderPassCreateInfo("FINAL_drill_typeScriptCreatedPass", 114514, new RenderPassAttachmentInfo(defaultColorAttachment, ClearBehavior.CLEAR_ONCE)));
  var pipelineBind = renderPass.createPipelineBind(0, pipeline);
  var renderTaskGroup = pipelineBind.createRenderTaskGroup([]);
  renderTaskGroup.addRenderTask(object);
})();
""");

            return new DrillPlugin();
        }
    }
}

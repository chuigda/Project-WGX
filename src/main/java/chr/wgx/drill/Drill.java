package chr.wgx.drill;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public final class Drill {
    private static final String SOURCE_CODE = """
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

  // src/packages/java.ts
  var IntArray = Java.type("int[]");
  var FloatArray = Java.type("float[]");
  var MemorySegment = java.lang.foreign.MemorySegment;
  function makeIntSegment(array) {
    var intArray = Java.to(array, IntArray);
    return MemorySegment.ofArray(intArray);
  }
  function makeFloatSegment(array) {
    var floatArray = Java.to(array, FloatArray);
    return MemorySegment.ofArray(floatArray);
  }

  // src/entry/drill.ts
  var objCreateInfo = new ObjectCreateInfo(new VertexInputInfo([
    new FieldInfoInput("position", CGType.Vec2),
    new FieldInfoInput("color", CGType.Vec3)
  ]), makeFloatSegment([
    // vec2 pos, vec3 color
    -0.5,
    -0.5,
    1,
    0,
    0,
    0.5,
    -0.5,
    0,
    1,
    0,
    0.5,
    0.5,
    0,
    0,
    1,
    -0.5,
    0.5,
    1,
    1,
    1
  ]), makeIntSegment([
    0,
    1,
    2,
    2,
    3,
    0
  ]));
  print(objCreateInfo);
})();
""";

    public static void main(String[] args) {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine("--language=es6");

        try {
            engine.eval(SOURCE_CODE);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}

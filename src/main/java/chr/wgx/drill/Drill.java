package chr.wgx.drill;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.*;

public final class Drill {
    private static final String SOURCE_CODE = """
"use strict";
(function() {
  // src/jvm.ts
  var System = java.lang.System;

  // src/wgx/tech.icey.xjbutil.ts
  var Pair = Java.type("tech.icey.xjbutil.container.Pair");
  var Option = Java.type("tech.icey.xjbutil.container.Option");

  // src/wgx/chr.wgx.render.ts
  var BlendMode = Java.type("chr.wgx.render.common.BlendMode");
  var CGType = Java.type("chr.wgx.render.common.CGType");
  var ClearBehavior = Java.type("chr.wgx.render.common.ClearBehavior");
  var PixelFormat = Java.type("chr.wgx.render.common.PixelFormat");
  var ShaderStage = Java.type("chr.wgx.render.common.ShaderStage");
  var UniformUpdateFrequency = Java.type("chr.wgx.render.common.UniformUpdateFrequency");
  var Color = Java.type("chr.wgx.render.common.Color");
  var AttachmentCreateInfo = Java.type("chr.wgx.render.info.AttachmentCreateInfo");

  // src/entry/drill.ts
  var p = new Pair("asd", "def");
  System.err.println(p.first());
  System.err.println(p.second());
  var opt1 = Option.some([
    114,
    514,
    1919,
    810
  ]);
  System.err.println(opt1);
  System.err.println(opt1.isSome());
  System.err.println("opt1.get() = " + opt1.get());
  var opt2 = Option.none();
  System.err.println(opt2);
  System.err.println(opt2.isNone());
  System.err.println("opt2.nullable() = " + opt2.nullable());
  System.err.println(UniformUpdateFrequency.PER_FRAME);
  System.err.println(new AttachmentCreateInfo(PixelFormat.RGBA_OPTIMAL, 1920, 1080));
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

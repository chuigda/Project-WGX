package chr.wgx.drill;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;

public final class Drill {
    private static final String SOURCE_CODE = """
"use strict";
(function() {
  // node_modules/.store/tslib@2.8.1/node_modules/tslib/tslib.es6.mjs
  function __generator(thisArg, body) {
    var _ = {
      label: 0,
      sent: function sent() {
        if (t[0] & 1) throw t[1];
        return t[1];
      },
      trys: [],
      ops: []
    }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() {
      return this;
    }), g;
    function verb(n) {
      return function(v) {
        return step([
          n,
          v
        ]);
      };
    }
    function step(op) {
      if (f) throw new TypeError("Generator is already executing.");
      while (g && (g = 0, op[0] && (_ = 0)), _) try {
        if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
        if (y = 0, t) op = [
          op[0] & 2,
          t.value
        ];
        switch (op[0]) {
          case 0:
          case 1:
            t = op;
            break;
          case 4:
            _.label++;
            return {
              value: op[1],
              done: false
            };
          case 5:
            _.label++;
            y = op[1];
            op = [
              0
            ];
            continue;
          case 7:
            op = _.ops.pop();
            _.trys.pop();
            continue;
          default:
            if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) {
              _ = 0;
              continue;
            }
            if (op[0] === 3 && (!t || op[1] > t[0] && op[1] < t[3])) {
              _.label = op[1];
              break;
            }
            if (op[0] === 6 && _.label < t[1]) {
              _.label = t[1];
              t = op;
              break;
            }
            if (t && _.label < t[2]) {
              _.label = t[2];
              _.ops.push(op);
              break;
            }
            if (t[2]) _.ops.pop();
            _.trys.pop();
            continue;
        }
        op = body.call(thisArg, _);
      } catch (e) {
        op = [
          6,
          e
        ];
        y = 0;
      } finally {
        f = t = 0;
      }
      if (op[0] & 5) throw op[1];
      return {
        value: op[0] ? op[1] : void 0,
        done: true
      };
    }
  }

  // src/jvm.ts
  var System = java.lang.System;

  // src/entry/generator.ts
  function main() {
    return __generator(this, function(_state) {
      switch (_state.label) {
        case 0:
          System.out.println("Zdravstvuyte, mir!");
          return [
            4,
            [1, 2, 3, 4]
          ];
        case 1:
          _state.sent();
          return [
            4,
            "\u5982\u68A6\u5E7B\u6CE1\u5F71"
          ];
        case 2:
          _state.sent();
          return [
            4,
            1314521
          ];
        case 3:
          _state.sent();
          return [
            4,
            "\u5E94\u4F5C\u5982\u662F\u89C2"
          ];
        case 4:
          _state.sent();
          return [
            2
          ];
      }
    });
  }

  const a = [1145, 2, 3, 114514]

  EntryPoint.register(main);
})();

""";

    public static final class EntryPoint {
        private @Nullable Object entryPoint = null;

        public void register(Object entryPoint) {
            this.entryPoint = entryPoint;
        }
    }

    public static void main(String[] args) {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine("--language=es6");
        Compilable compilable = (Compilable) engine;

        try {
            EntryPoint ep = new EntryPoint();

            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("EntryPoint", ep);

            engine.eval(SOURCE_CODE);

            bindings.put("main", ep.entryPoint);
            ScriptObjectMirror state = (ScriptObjectMirror) engine.eval("main()", bindings);

            CompiledScript stepScript = compilable.compile("state.next()");
            bindings.put("state", state);

            while (true) {
                state = (ScriptObjectMirror) stepScript.eval(bindings);
                Object value = state.get("value");
                boolean done = (boolean) state.get("done");
                if (done) {
                    break;
                }

                System.err.println("state.next() = { value: " + value + " (of type" + value.getClass().getCanonicalName() + ") }");
            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}

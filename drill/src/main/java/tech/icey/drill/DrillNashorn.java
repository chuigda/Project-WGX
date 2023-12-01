package tech.icey.drill;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.*;

public class DrillNashorn {
    public static void main(String[] args) {
        ScriptEngine engine = new NashornScriptEngineFactory()
                .getScriptEngine("--language=es6");

        try {
            engine.eval("""
const applicationStart = () => {
    const System = java.lang.System
    System.out.println("Hello, World!")
}

applicationStart()
""");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}

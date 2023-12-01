package tech.icey.drill;

import javax.script.*;

public class DrillNashorn {
    public static void main(String[] args) {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");

        try {
            engine.eval("""
var System = java.lang.System;
System.out.println("Hello, World!");
""");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}

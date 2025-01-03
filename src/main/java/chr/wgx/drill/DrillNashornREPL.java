package chr.wgx.drill;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;

public final class DrillNashornREPL {
    public static void main(String[] args) {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ScriptEngine engine = factory.getScriptEngine("--language=es6");

        try {
            System.out.print("> ");
            @Nullable String line = System.console().readLine();
            while (line != null) {
                try {
                    Object result = engine.eval(line);
                    System.out.println(result);
                } catch (Exception e) {
                    System.err.println("error: " + e.getMessage());
                }
                System.out.print("> ");
                line = System.console().readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

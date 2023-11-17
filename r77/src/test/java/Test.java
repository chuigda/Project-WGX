import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import tech.icey.r77.meta.VertexProcessor;

import static com.google.testing.compile.Compiler.javac;

public class Test {
    public static void main(String[] args) {
        Compilation compilation = javac()
                .withProcessors(new VertexProcessor())
                .compile(JavaFileObjects.forResource("MyVertex.java"));

        System.err.println(compilation.errors());
    }
}

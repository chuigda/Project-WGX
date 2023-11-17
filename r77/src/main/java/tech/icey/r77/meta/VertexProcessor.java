package tech.icey.r77.meta;

import com.google.auto.service.AutoService;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("tech.icey.r77.meta.Vertex")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class VertexProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            System.out.println(annotation.getQualifiedName());

            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            List<TypeElement> classes = annotatedElements
                    .stream()
                    .map(element -> (TypeElement) element)
                    .sorted((o1, o2) -> {
                        Attribute a1 = o1.getAnnotation(Attribute.class);
                        Attribute a2 = o2.getAnnotation(Attribute.class);
                        return a1.position() - a2.position();
                    }).toList();

            for (TypeElement clazz : classes) {
                // read all fields with annotation "Attribute"
                // and generate code
                System.out.println(clazz.getQualifiedName());
                for (Element e : clazz.getEnclosedElements()) {
                    Attribute attribute = e.getAnnotation(Attribute.class);
                    if (attribute != null) {
                        System.out.println("\tfield: @Attribute(name = \"" +
                                attribute.name() +
                                "\", position = " +
                                attribute.position() +
                                ") " +
                                e.getSimpleName()
                        );
                    }
                }
            }
        }

        return false;
    }
}

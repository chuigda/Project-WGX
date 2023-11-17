package tech.icey.r77.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import tech.icey.util.NotNull;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Attribute {
    @NotNull String name();
    int position();
}

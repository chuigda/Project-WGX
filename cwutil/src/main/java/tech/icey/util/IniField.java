package tech.icey.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IniField {
    String section() default "default";
    String key() default "";
    String defaultValue() default "";
}

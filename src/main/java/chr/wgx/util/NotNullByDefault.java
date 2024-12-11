package chr.wgx.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("NullableProblems")
@Documented
@NotNull
@TypeQualifierDefault({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.MODULE,
        ElementType.PARAMETER,
        ElementType.TYPE
})
@Retention(RetentionPolicy.SOURCE)
public @interface NotNullByDefault {}

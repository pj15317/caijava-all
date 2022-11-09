package cn.caijava.core.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD})
@Retention(RUNTIME)
@Repeatable(BeanFieldRef.List.class)
public @interface BeanFieldRef {

    Class<?> refResultClass();
    String refResultFieldName();


    @Target({METHOD})
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        BeanFieldRef[] value();
    }
}

package cn.caijava.core.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoundOff {
    int scale() default 2;

    RoundingMode roundingMode() default RoundingMode.HALF_UP;
}

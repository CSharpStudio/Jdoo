package org.jdoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 应用清单声明
 * 
 * @author lrz
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manifest {

    String name();

    String summary() default "";

    String description() default "";

    String version() default "";

    String category() default "";

    String[] depends() default {};

    String[] data() default {};

    boolean installable() default false;

    boolean autoInstall() default false;

    String license() default "";

    String author() default "";

    Class<?>[] models() default {};

    boolean application() default true;
}

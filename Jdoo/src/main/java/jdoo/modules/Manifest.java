package jdoo.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manifest {
    String name();

    String version();

    String summary() default "";

    String category() default "";

    String website() default "";

    String description() default "";

    String[] images() default {};

    String[] depends() default {};

    String[] data() default {};

    String[] demo() default {};

    String[] test() default {};

    boolean installable();

    boolean auto_install();

    String post_init_hook() default "";

}

package org.jdoo.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import org.jdoo.Model;

/**
 * @author lrz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelServices {
    /**
     * 值
     * 
     * @return
     */
    Model.Service[] value();
}

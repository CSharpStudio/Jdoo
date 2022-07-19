package org.jdoo.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jdoo.Model;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lrz
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelUniqueConstraints {
    /**
     * 值
     * 
     * @return
     */
    Model.UniqueConstraint[] value();
}

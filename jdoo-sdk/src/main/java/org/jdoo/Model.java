package org.jdoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jdoo.annotation.ModelServices;
import org.jdoo.annotation.ModelUniqueConstraints;

import java.lang.annotation.Repeatable;

/**
 * 模型
 * 
 * @author lrz
 */
public class Model extends AbstractModel {
    public Model() {
        isAuto = true;
        isAbstract = false;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Meta {
        /**
         * 模型的名称(可选)
         * 
         * @return
         */
        String name() default "";

        /**
         * 模型的标题(可选)
         * 
         * @return
         */
        String label() default "";

        /**
         * 模型的描述(可选)
         * 
         * @return
         */
        String description() default "";

        /**
         * 继承的模型, 支持多继承
         * 
         * @return
         */
        String[] inherit() default {};

        /**
         * 模型映射的表名(可选)
         * 
         * @return
         */
        String table() default "";

        /**
         * 查询默认排序(可选)
         * 
         * @return
         */
        String order() default "";

        /**
         * 记录呈现的字段名(可选)
         */
        String[] present() default {};

        /**
         * 记录呈现的格式化
         */
        String presentFormat() default "";

        /**
         * 是否记录访问信息(可选)
         * 
         * @return
         */
        BoolState logAccess() default BoolState.None;
    }

    @Repeatable(ModelServices.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Service {
        /**
         * 服务名称
         * 
         * @return
         */
        String name() default "";

        /**
         * 标题
         */
        String label() default "";

        /**
         * 授权
         */
        String auth() default "";

        /**
         * 详细说明
         * 
         * @return
         */
        String description() default "";

        /**
         * 服务类型,必须是{@link org.jdoo.Service}子类
         * 
         * @return
         */
        Class<?> type() default Void.class;

        /**
         * 移除的服务名称, 指定移除时，其它参数失效
         * 
         * @return
         */
        String remove() default "";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ServiceMethod {
        /**
         * 服务方法名称(可选)
         * 
         * @return
         */
        String name() default "";

        /**
         * 标题
         */
        String label() default "";

        /**
         * 授权
         */
        String auth() default "";

        /**
         * 方法描述
         * 
         * @return
         */
        String doc() default "";

        /**
         * 是否操作记录，如果是，需提供id集合，否则为空集合
         */
        boolean records() default true;
    }

    /**
     * @author lrz
     */
    @Repeatable(ModelUniqueConstraints.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UniqueConstraint {
        /**
         * 唯一约束名称
         * 
         * @return
         */
        String name();

        /**
         * 字段
         * 
         * @return
         */
        String[] fields();

        /**
         * 提示信息(可选)
         * 
         * @return
         */
        String message() default "";
    }

    /**
     * @author lrz
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Constrains {
        /**
         * 约束的字段
         * 
         * @return
         */
        String[] value();
    }
}
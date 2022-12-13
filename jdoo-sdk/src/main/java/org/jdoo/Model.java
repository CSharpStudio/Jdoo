package org.jdoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jdoo.annotation.ModelServices;
import org.jdoo.annotation.ModelUniqueConstraints;
import org.jdoo.core.BaseModel;

import java.lang.annotation.Repeatable;

/**
 * 模型基类
 * <blockquote>
 * 
 * <pre>
 * 最简单的情况下，构建后的模型'继承'自平面层次结构中定义的模型
 *
 *   {@code @}Sdk.Model(name="a")                             Model
 *   {@code }class A1 extends Model{ }                        / | \
 *   {@code }                                                A3 A2 A1
 *   {@code @}Sdk.Model(name="a", inherit="a")                \ | /
 *   {@code }class A2 extends Model{ }                       MetaModel
 *   {@code }                                                 /   \
 *   {@code @}Sdk.Model(name="a", inherit="a")            model   RecordSet
 *   {@code }class A3 extends Model{ }
 *
 * 当一个模型被'inherit'扩展时，它的基类被修改包含当前类和其他继承的模型类。
 * 注意，模型实际是继承自其它构建好的模型，所以被继承的模型有扩展时，都会解析到到子模型
 *
 *   {@code @}Sdk.Model(name="a")
 *   {@code }class A1 extends Model{ }                         Model
 *   {@code }                                                 / / \ \
 *   {@code @}Sdk.Model(name="b")                            / A2 A1 \
 *   {@code }class B1 extends Model{ }                      /   \ /   \
 *   {@code }                                              B2  ModelA  B1
 *   {@code @}Sdk.Model(name="b", inherit={"a","b"})        \    |    /
 *   {@code }class B2 extends Model{ }                       \   |   /
 *   {@code }                                                 \  |  /
 *   {@code @}Sdk.Model(name="a", inherit="a")                 ModelB
 *   {@code }class A2 extends Model{ }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author lrz
 */
public class Model extends BaseModel {
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
         * 授权模型的名称，权限码read按此模型判断权限，角色权限配置时与此模型一起显示
         * 
         * @return
         */
        String authModel() default "";

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
         * 记录展示的字段名(可选)
         */
        String[] present() default {};

        /**
         * 记录展示的格式化，使用{}限定字段，如:{code}-{name}
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
         * 移除的服务名称, 指定移除时，其它参数将失效，使用@all移除所有服务
         * 
         * @return
         */
        String[] remove() default {};
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
        String[] value() default {};
    }
}
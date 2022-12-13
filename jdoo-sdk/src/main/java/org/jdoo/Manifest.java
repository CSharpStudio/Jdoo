package org.jdoo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模块清单声明，应用包含一个或多个模块，应用安装的时候自动把依赖的模块一起安装。
 * 
 * @author lrz
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Manifest {

    /**
     * 模块名称
     * 
     * @return
     */
    String name();

    /**
     * 标题
     * 
     * @return
     */
    String label();

    /**
     * 描述说明
     * 
     * @return
     */
    String description() default "";

    /**
     * 版本，只用于标识，模块的依赖跟版本无关
     * 
     * @return
     */
    String version() default "";

    /**
     * 模块分类
     * 
     * @return
     */
    String category() default "";

    /**
     * 模块的依赖，其它模块的名称
     * 
     * @return
     */
    String[] depends() default {};

    /**
     * 数据，在应用安装时自动更新数据。如：视图定义、种子数据
     * 
     * @return
     */
    String[] data() default {};

    /**
     * 是否自动安装
     * 
     * @return
     */
    boolean autoInstall() default false;

    /**
     * 授权协议
     * 
     * @return
     */
    String license() default "";

    /**
     * 作者
     * 
     * @return
     */
    String author() default "";

    /**
     * 模块中定义的模型，模型有继承关系的，应该先定义父模型，再定义子模型
     * 
     * @return
     */
    Class<?>[] models() default {};

    /**
     * 模块中定义的控制器，模块初化时自动注册
     * 
     * @return
     */
    Class<?>[] controllers() default {};

    /**
     * 注册表initModels时执行的方法，格式 model::method, 例如 ir.module::postInit
     * 
     * @return
     */
    String postInit() default "";

    /**
     * 是否应用
     * 
     * @return
     */
    boolean application() default true;

    /**
     * 图标, 没指定时，自动尝试加载statics目录下的icon.png
     */
    String icon() default "";
}

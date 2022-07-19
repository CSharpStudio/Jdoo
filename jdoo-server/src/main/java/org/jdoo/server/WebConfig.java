package org.jdoo.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.jdoo.https.ControllerInterceptor;

/**
 * web配置
 * 
 * @author lrz
 */
@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    ControllerInterceptor controllerInterceptor() {
        return new ControllerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerInterceptor());
    }

    // /**
    //  * 添加静态资源文件，外部可以直接访问地址
    //  *
    //  * @param registry
    //  */
    // @Override
    // public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //     registry.addResourceHandler("**/static/**").addResourceLocations("classpath*:**/static/");
    // }
}
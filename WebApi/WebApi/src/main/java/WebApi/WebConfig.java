package WebApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jdoo.https.ControllerInterceptor;

@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer  {

    @Bean
    ControllerInterceptor controllerInterceptor() {
         return new ControllerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerInterceptor());
    }

}
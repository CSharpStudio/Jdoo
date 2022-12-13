package org.jdoo.utils;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Spring ioc 工具包
 */
public class SpringUtils {
    final static Logger logger = LoggerFactory.getLogger(SpringUtils.class);

    static ConfigurableApplicationContext applicationContext;

    /**
     * 设置应用上下文
     * 
     * @param ctx
     */
    public static void setApplicationContext(ConfigurableApplicationContext ctx) {
        applicationContext = ctx;
    }

    /**
     * 注册bean
     * 
     * @param clazz
     * @return
     * @throws Exception
     */
    public static String registerBean(Class<?> clazz) throws Exception {
        String beanName = clazz.getName();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setScope("singleton");
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
        return beanName;
    }

    public static void registerController(Class<?> clazz) throws Exception {
        String beanName = clazz.getName();
        if (!applicationContext.containsBean(beanName)) {
            registerController(registerBean(clazz));
        }
    }

    /**
     * 注册Controller
     * 
     * @param controllerBeanName
     * @throws Exception
     */
    public static void registerController(String controllerBeanName) throws Exception {
        if (!applicationContext.containsBean(controllerBeanName)) {
            logger.warn("注册控制器失败，未注册Bean[{}]", controllerBeanName);
            return;
        }
        final RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean(RequestMappingHandlerMapping.class);
        if (requestMappingHandlerMapping != null) {
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass()
                    .getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, controllerBeanName);
        }
    }

    /**
     * 去掉Controller的Mapping
     * 
     * @param clazz
     */
    public static void unregisterController(Class<?> clazz) {
        unregisterController(clazz.getName());
    }

    /**
     * 去掉Controller的Mapping
     * 
     * @param controllerBeanName
     */
    public static void unregisterController(String controllerBeanName) {
        if (!applicationContext.containsBean(controllerBeanName)) {
            return;
        }
        Object controller = applicationContext.getBean(controllerBeanName);
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext
                .getBean("requestMappingHandlerMapping");
        if (requestMappingHandlerMapping != null) {
            final Class<?> targetClass = controller.getClass();
            ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    try {
                        Method createMappingMethod = RequestMappingHandlerMapping.class
                                .getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                        createMappingMethod.setAccessible(true);
                        RequestMappingInfo requestMappingInfo = (RequestMappingInfo) createMappingMethod
                                .invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                        if (requestMappingInfo != null) {
                            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }        
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        beanFactory.removeBeanDefinition(controllerBeanName);
    }
}

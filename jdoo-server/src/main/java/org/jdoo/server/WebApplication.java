package org.jdoo.server;

import org.jdoo.utils.SpringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * web应用
 * 
 * @author lrz
 */
@SpringBootApplication
@ComponentScan(basePackages = { "org.jdoo"/* , "**.addons" */ })
public class WebApplication {

	public static void main(String[] args) {
		SpringUtils.setApplicationContext(SpringApplication.run(WebApplication.class, args));
	}
}

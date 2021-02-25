package WebApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jdoo.util.Kvalues;

//@EnableAutoConfiguration
@RestController
@SpringBootApplication
@ComponentScan(basePackageClasses = { jdoo.addons.web.controllers.DataSetController.class })
@ComponentScan("WebApi")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Kvalues hello() {
        Kvalues map = new Kvalues();
        map.put("Hello World", 1.0);
        map.put("Hello Java", 1.1);
        map.put("Hello Odoo", 1.2);
        return map;
    }
}

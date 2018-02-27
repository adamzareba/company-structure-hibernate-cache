package com.adamzareba.hibernate.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import static org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;

@SpringBootApplication(exclude = JmxAutoConfiguration.class)
@EnableLoadTimeWeaving(aspectjWeaving = AspectJWeaving.ENABLED)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

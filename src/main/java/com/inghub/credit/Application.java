package com.inghub.credit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAutoConfiguration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.inghub.credit")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
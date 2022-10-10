package ru.home.hpsmspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
//@EnableAsync(proxyTargetClass = true)

public class HpsmSpringApplication {

    public static void main(String[] args) {


        SpringApplication.run(HpsmSpringApplication.class, args);


    }

//    @Bean
//    public TaskScheduler taskScheduler() {
//        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//        scheduler.setPoolSize(2);
//        return scheduler;
//    }
}

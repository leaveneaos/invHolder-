package com.rjxx;

import com.rjxx.comm.mybatis.MybatisRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by admin on 2016/1/6.
 */
@SpringBootApplication
@MapperScan(annotationClass = MybatisRepository.class, basePackages = {"com.rjxx"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

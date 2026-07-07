package com.panel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.panel.mapper")
public class PanelApplication {
    public static void main(String[] args) {
        SpringApplication.run(PanelApplication.class, args);
    }
}

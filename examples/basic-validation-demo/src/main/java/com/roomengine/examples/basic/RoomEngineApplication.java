package com.roomengine.examples.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.roomengine.examples.basic")
public class RoomEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoomEngineApplication.class, args);
    }
}

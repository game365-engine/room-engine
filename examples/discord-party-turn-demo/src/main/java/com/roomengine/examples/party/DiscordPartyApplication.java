package com.roomengine.examples.party;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.roomengine.examples.party")
public class DiscordPartyApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscordPartyApplication.class, args);
    }
}

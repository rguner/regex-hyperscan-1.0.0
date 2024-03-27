package com.guner.regexhyperscan.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CommandLine {

    @Bean
    public CommandLineRunner run() {
        return args -> {
            System.out.println("started....");
        };
    }
}
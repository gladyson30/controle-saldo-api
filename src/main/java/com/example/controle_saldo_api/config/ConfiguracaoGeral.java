package com.example.controle_saldo_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ConfiguracaoGeral {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

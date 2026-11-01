package com.journeyflow.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio injetável: os services usam Clock em vez de LocalDate.now()/OffsetDateTime.now(),
 * o que permite fixar o "agora" nos testes (streak, answeredAt, expiração do JWT...).
 * O fuso segue o da JVM; em produção, rode com -Duser.timezone=America/Sao_Paulo.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

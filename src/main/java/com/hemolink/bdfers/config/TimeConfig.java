package com.hemolink.bdfers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides the application clock. Date-dependent rules (age, days since
 * last donation, CBC age) use this bean instead of LocalDate.now(), so
 * tests can supply a fixed reference date (NFR-07).
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

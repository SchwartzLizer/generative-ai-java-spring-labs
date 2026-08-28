package com.schwartzlizer.support.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class CommonConfiguration {
    @Bean Clock applicationClock() { return Clock.systemUTC(); }
    @Bean Supplier<UUID> uuidSupplier() { return UUID::randomUUID; }
}

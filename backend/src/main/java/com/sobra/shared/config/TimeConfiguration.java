package com.sobra.shared.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

    @Bean
    Clock businessClock(@Value("${sobra.business-zone}") String businessZone) {
        return Clock.system(ZoneId.of(businessZone));
    }
}

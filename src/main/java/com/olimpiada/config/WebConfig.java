package com.olimpiada.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToDurationConverter());
    }

    private static class StringToDurationConverter implements Converter<String, Duration> {
        @Override
        public Duration convert(String source) {
            try {
                long minutes = Long.parseLong(source);
                return Duration.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
} 
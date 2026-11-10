package com.joutio.theme.infrastructure.config;

import com.joutio.theme.domain.model.QuestionAutoSuspension;
import com.joutio.theme.infrastructure.properties.QuestionReportProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expose le réglage de suspension automatique à la couche application, sans dépendre des propriétés. */
@Configuration
public class QuestionReportConfig {

    @Bean
    QuestionAutoSuspension questionAutoSuspension(QuestionReportProperties properties) {
        QuestionReportProperties.AutoSuspension auto = properties.autoSuspension();
        if (auto == null || !auto.enabled()) {
            return QuestionAutoSuspension.DISABLED;
        }
        return new QuestionAutoSuspension(true, auto.threshold(), auto.window());
    }
}

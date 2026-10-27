package com.joutio.theme;

import com.joutio.theme.infrastructure.properties.AppProperties;
import com.joutio.theme.infrastructure.properties.QuestionReportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, QuestionReportProperties.class})
public class ThemeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThemeServiceApplication.class, args);
    }
}

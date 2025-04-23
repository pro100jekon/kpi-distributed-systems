package ua.kpi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ServiceUrlConfiguration.ServiceUrlProperties.class)
public class ServiceUrlConfiguration {

    @ConfigurationProperties("config")
    public record ServiceUrlProperties(Map<String, List<String>> urls) {
    }
}

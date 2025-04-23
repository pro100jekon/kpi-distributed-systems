package ua.kpi.distributedsystems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import ua.kpi.distributedsystems.client.LoggingClient;
import ua.kpi.distributedsystems.client.LoggingClientImpl;
import ua.kpi.distributedsystems.client.MessagingClient;
import ua.kpi.distributedsystems.client.MessagingClientImpl;

import java.util.List;

@Configuration
public class ClientConfiguration {

    @Value("${config-server-url}")
    private String configServerUrl;

    @Bean
    public MessagingClient messagingClient() {
        return new MessagingClientImpl(getConfiguration("messages-service"));
    }

    @Bean
    public LoggingClient loggingClient() {
        return new LoggingClientImpl(getConfiguration("logging-service"));
    }

    private List<String> getConfiguration(String serviceName) {
        return WebClient.create()
                .get()
                .uri(configServerUrl + "/" + serviceName)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                })
                .block();
    }
}

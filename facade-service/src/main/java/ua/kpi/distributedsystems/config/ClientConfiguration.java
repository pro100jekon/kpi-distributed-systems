package ua.kpi.distributedsystems.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.kpi.distributedsystems.client.LoggingClient;
import ua.kpi.distributedsystems.client.LoggingClientImpl;
import ua.kpi.distributedsystems.client.MessagingClient;
import ua.kpi.distributedsystems.client.MessagingClientImpl;

@Configuration
public class ClientConfiguration {

    @Bean
    public MessagingClient messagingClient(DiscoveryClient discoveryClient) {
        return new MessagingClientImpl(discoveryClient);
    }

    @Bean
    public LoggingClient loggingClient(DiscoveryClient discoveryClient) {
        return new LoggingClientImpl(discoveryClient);
    }
}

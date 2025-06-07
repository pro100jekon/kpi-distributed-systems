package ua.kpi.distributedsystems.client;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.downstream.MessageResponse;

public class MessagingClientImpl implements MessagingClient {

    private final WebClient webClient;

    public MessagingClientImpl(DiscoveryClient discoveryClient) {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .filter(new RoundRobinExchangeFilter(discoveryClient, "messaging-service"))
                .build();
    }

    @Override
    public Mono<MessageResponse> getMessage() {
        return webClient.get()
                .uri("/messaging")
                .retrieve()
                .bodyToMono(MessageResponse.class);
    }
}

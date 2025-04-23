package ua.kpi.distributedsystems.client;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.downstream.MessageResponse;

import java.util.List;

public class MessagingClientImpl implements MessagingClient {

    private final WebClient webClient;

    public MessagingClientImpl(List<String> hosts) {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .filter(new RoundRobinExchangeFilter(hosts))
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

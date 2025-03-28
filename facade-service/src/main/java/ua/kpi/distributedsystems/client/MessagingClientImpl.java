package ua.kpi.distributedsystems.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.downstream.MessageResponse;

@Component
public class MessagingClientImpl implements MessagingClient {

    private final WebClient webClient;

    public MessagingClientImpl(@Value("${http-client.messaging-service-url}") String messagingServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(messagingServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public Mono<MessageResponse> getMessage() {
        return webClient.get()
                .retrieve()
                .bodyToMono(MessageResponse.class);
    }
}

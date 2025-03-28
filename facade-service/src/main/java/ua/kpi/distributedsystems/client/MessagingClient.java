package ua.kpi.distributedsystems.client;

import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.downstream.MessageResponse;

public interface MessagingClient {

    Mono<MessageResponse> getMessage();
}

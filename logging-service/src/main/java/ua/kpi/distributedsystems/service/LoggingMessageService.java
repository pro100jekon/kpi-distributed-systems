package ua.kpi.distributedsystems.service;

import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.LogMessageDto;

import java.util.List;
import java.util.UUID;

public interface LoggingMessageService {

    Mono<Void> logMessage(UUID uuid, String msg);

    Mono<List<String>> getMessages();
}

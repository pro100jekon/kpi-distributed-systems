package ua.kpi.distributedsystems.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.client.LoggingClient;
import ua.kpi.distributedsystems.client.MessagingClient;
import ua.kpi.distributedsystems.model.downstream.LogMessageRequest;
import ua.kpi.distributedsystems.model.dto.LogMessageResponseDto;

import java.util.UUID;

@Service
public class FacadeServiceImpl implements FacadeService {

    private final LoggingClient loggingClient;
    private final MessagingClient messagingClient;

    public FacadeServiceImpl(LoggingClient loggingClient, MessagingClient messagingClient) {
        this.loggingClient = loggingClient;
        this.messagingClient = messagingClient;
    }

    @Override
    public Mono<Void> writeLog(String msg) {
        return loggingClient
                .writeLog(new LogMessageRequest(UUID.randomUUID(), msg));
    }

    @Override
    public Mono<LogMessageResponseDto> getLogsMessages() {
        return Mono.zip(loggingClient.getLog(), messagingClient.getMessage())
                .map(tuple -> new LogMessageResponseDto(tuple.getT1().msg(), tuple.getT2().msg()));
    }
}

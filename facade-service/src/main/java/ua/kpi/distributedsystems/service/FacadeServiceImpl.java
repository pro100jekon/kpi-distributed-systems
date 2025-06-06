package ua.kpi.distributedsystems.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.client.LoggingClient;
import ua.kpi.distributedsystems.client.MessagingClient;
import ua.kpi.distributedsystems.model.downstream.LogMessageRequest;
import ua.kpi.distributedsystems.model.dto.LogMessageResponseDto;
import ua.kpi.distributedsystems.model.dto.MessageDto;

import java.util.UUID;

@Service
public class FacadeServiceImpl implements FacadeService {

    private final LoggingClient loggingClient;
    private final MessagingClient messagingClient;
    private final MessagesProducerService messagesProducerService;

    public FacadeServiceImpl(LoggingClient loggingClient, MessagingClient messagingClient, MessagesProducerService messagesProducerService) {
        this.loggingClient = loggingClient;
        this.messagingClient = messagingClient;
        this.messagesProducerService = messagesProducerService;
    }

    @Override
    public Mono<Void> writeLog(String msg) {
        var uuid = UUID.randomUUID();
        return loggingClient
                .writeLog(new LogMessageRequest(uuid, msg))
                .then(messagesProducerService.sendMessage(uuid, new MessageDto(msg)))
                .then();
    }

    @Override
    public Mono<LogMessageResponseDto> getLogsMessages() {
        return Mono.zip(loggingClient.getLog(), messagingClient.getMessage())
                .map(tuple -> new LogMessageResponseDto(tuple.getT1().msg(), tuple.getT2().msg()));
    }
}

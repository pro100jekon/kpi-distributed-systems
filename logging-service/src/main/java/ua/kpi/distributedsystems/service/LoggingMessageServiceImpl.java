package ua.kpi.distributedsystems.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.LogMessageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoggingMessageServiceImpl implements LoggingMessageService {

    private static final Map<UUID, String> MESSAGES = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(LoggingMessageServiceImpl.class);

    @Override
    public Mono<Void> logMessage(UUID uuid, String msg) {
        return Mono.fromRunnable(() -> {
            var res = MESSAGES.putIfAbsent(uuid, msg);
            if (res == null) {
                log.info("Successfully stored a new message. {}: {}", uuid, msg);
            }
        });
    }

    @Override
    public Mono<List<String>> getMessages() {
        return Mono.just(new ArrayList<>(MESSAGES.values()));
    }
}

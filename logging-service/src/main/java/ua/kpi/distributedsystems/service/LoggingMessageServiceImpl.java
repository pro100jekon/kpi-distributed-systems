package ua.kpi.distributedsystems.service;

import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LoggingMessageServiceImpl implements LoggingMessageService {

    private static final Logger log = LoggerFactory.getLogger(LoggingMessageServiceImpl.class);

    private final IMap<UUID, String> messages;

    public LoggingMessageServiceImpl(@Autowired @Qualifier("map") IMap<UUID, String> map) {
        this.messages = map;
    }

    @Override
    public Mono<Void> logMessage(UUID uuid, String msg) {
        return Mono.fromRunnable(() -> {
            var res = messages.putIfAbsent(uuid, msg);
            if (res == null) {
                log.info("Successfully stored a new message. {}: {}", uuid, msg);
            }
        });
    }

    @Override
    public Mono<List<String>> getMessages() {
        return Mono.just(new ArrayList<>(messages.values()));
    }
}

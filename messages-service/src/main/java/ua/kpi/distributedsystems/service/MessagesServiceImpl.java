package ua.kpi.distributedsystems.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ua.kpi.distributedsystems.model.dto.MessageDto;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessagesServiceImpl implements MessagesService {

    private static final Logger log = LoggerFactory.getLogger(MessagesServiceImpl.class);

    private static final ConcurrentHashMap<String, MessageDto> messages = new ConcurrentHashMap<>();

    // @KafkaListener(topics = "messages", groupId = "message-consumer-group")
    @KafkaListener(topics = "messages", groupId = "message-consumer-group-2")
    @Override
    public void consumeMessage(@Header(KafkaHeaders.RECEIVED_KEY) String key,
                               @Payload MessageDto messageDto) {
        log.info("Received {}:{}", key, messageDto.msg());
        messages.put(key, messageDto);
    }

    @Override
    public ConcurrentHashMap<String, MessageDto> getMessages() {
        return messages;
    }
}

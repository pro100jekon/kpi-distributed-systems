package ua.kpi.distributedsystems.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ua.kpi.distributedsystems.model.dto.MessageDto;

import java.util.UUID;

@Service
public class MessagesProducerService {

    private static final Logger log = LoggerFactory.getLogger(MessagesProducerService.class);

    private final KafkaTemplate<String, MessageDto> kafkaTemplate;

    private static final String TOPIC = "messages";

    public MessagesProducerService(KafkaTemplate<String, MessageDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<String> sendMessage(UUID uuid, MessageDto messageDto) {
        return Mono.fromCallable(() -> {
                    var result = kafkaTemplate.send(TOPIC, uuid.toString(), messageDto).get();
                    var offset = result.getRecordMetadata().offset();
                    var partition = result.getRecordMetadata().partition();
                    log.info("Message sent at offset: {}, partition {}", offset, partition);
                    return (String) null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.error("Failed to send message: {}", error.getMessage()));
    }
}

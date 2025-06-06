package ua.kpi.distributedsystems.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.MessageDto;
import ua.kpi.distributedsystems.service.MessagesService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("messaging")
public class MessagingController {

    private final MessagesService messagesService;

    public MessagingController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @GetMapping
    public Mono<MessageDto> getMessages() {
        return Mono.just(new MessageDto(messagesService.getMessages().values()
                .stream()
                .map(MessageDto::msg)
                .collect(Collectors.joining(","))));
    }
}

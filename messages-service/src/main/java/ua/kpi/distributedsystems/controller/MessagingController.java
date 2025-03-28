package ua.kpi.distributedsystems.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.MessageDto;

@RestController
@RequestMapping("messaging")
public class MessagingController {

    @GetMapping
    public Mono<MessageDto> getMessages() {
        return Mono.just(new MessageDto("not implemented yet"));
    }
}

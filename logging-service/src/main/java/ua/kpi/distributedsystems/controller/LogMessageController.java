package ua.kpi.distributedsystems.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.LogMessageDto;
import ua.kpi.distributedsystems.service.LoggingMessageService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("logging")
public class LogMessageController {

    private final LoggingMessageService loggingMessageService;

    public LogMessageController(LoggingMessageService loggingMessageService) {
        this.loggingMessageService = loggingMessageService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> logMessage(@RequestBody LogMessageDto logMessageDto) {
        return loggingMessageService.logMessage(logMessageDto.uuid(), logMessageDto.msg());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    private Mono<LogMessageDto> getAllMessages() {
        return loggingMessageService.getMessages()
                .map(messages -> new LogMessageDto(null,
                        messages.stream().collect(Collectors.joining(", ", "[", "]"))));
    }
}

package ua.kpi.distributedsystems.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.LogMessageResponseDto;
import ua.kpi.distributedsystems.model.dto.LogRequestDto;
import ua.kpi.distributedsystems.service.FacadeService;

@RestController
@RequestMapping("facade")
public class FacadeController {

    private final FacadeService facadeService;

    public FacadeController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }

    @PostMapping(value = "write-log", consumes = "application/json")
    public Mono<Void> writeLog(@RequestBody LogRequestDto request) {
        return facadeService.writeLog(request.msg());
    }

    @GetMapping(value = "logs-messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<LogMessageResponseDto> getLogsMessages() {
        return facadeService.getLogsMessages();
    }
}

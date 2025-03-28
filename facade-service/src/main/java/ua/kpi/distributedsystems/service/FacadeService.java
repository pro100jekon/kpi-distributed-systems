package ua.kpi.distributedsystems.service;

import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.dto.LogMessageResponseDto;

public interface FacadeService {

    Mono<Void> writeLog(String msg);

    Mono<LogMessageResponseDto> getLogsMessages();
}

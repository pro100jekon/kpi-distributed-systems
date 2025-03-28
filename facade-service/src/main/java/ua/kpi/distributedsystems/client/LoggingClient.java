package ua.kpi.distributedsystems.client;

import reactor.core.publisher.Mono;
import ua.kpi.distributedsystems.model.downstream.LogMessageRequest;
import ua.kpi.distributedsystems.model.downstream.LogResponse;

public interface LoggingClient {

    Mono<Void> writeLog(LogMessageRequest request);

    Mono<LogResponse> getLog();
}

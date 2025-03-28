package ua.kpi.distributedsystems.model.downstream;

import java.util.UUID;

public record LogMessageRequest(UUID uuid, String msg) {
}

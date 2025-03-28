package ua.kpi.distributedsystems.model.dto;

import java.util.UUID;

public record LogMessageDto(UUID uuid, String msg) {
}

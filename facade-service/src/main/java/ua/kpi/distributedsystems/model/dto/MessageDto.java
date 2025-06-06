package ua.kpi.distributedsystems.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDto(@JsonProperty String msg) {
}

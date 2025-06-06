package ua.kpi.distributedsystems.service;

import ua.kpi.distributedsystems.model.dto.MessageDto;

import java.util.concurrent.ConcurrentHashMap;

public interface MessagesService {

    void consumeMessage(String key, MessageDto messageDto);

    ConcurrentHashMap<String, MessageDto> getMessages();
}

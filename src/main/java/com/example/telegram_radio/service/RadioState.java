package com.example.telegram_radio.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.telegram_radio.config.TelegramProperties;

@Component
public class RadioState {

    private static final int MAX_MESSAGES = 100;

    private final TelegramProperties props;
    private final Deque<ReceivedMessage> messages = new ArrayDeque<>();
    private volatile String lastChatId;
    private long counter = 0;

    public RadioState(TelegramProperties props) {
        this.props = props;
    }

    public Optional<String> chatId() {
        String configured = props.chatId();
        if (configured != null && !configured.isBlank()) {
            return Optional.of(configured);
        }

        return Optional.ofNullable(lastChatId);
    }

    public void rememberChat(String chatId) {
        this.lastChatId = chatId;
    }

    public synchronized ReceivedMessage add(String from, String text) {
        ReceivedMessage message = new ReceivedMessage(++counter, from, text, Instant.now());
        messages.addLast(message);
        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
        return message;
    }

    public synchronized List<ReceivedMessage> after(long id) {
        return messages.stream().filter(m -> m.id() > id).toList();
    }

    public record ReceivedMessage(long id, String from, String text, Instant time) {

    }

}

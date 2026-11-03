package com.example.telegram_radio.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.example.telegram_radio.service.RadioState;
import com.example.telegram_radio.service.TelegramClient;

@RestController
@RequestMapping("/api")
public class RadioController {

    private static final Logger log = LoggerFactory.getLogger(RadioController.class);

    private final TelegramClient client;
    private final RadioState state;

    public RadioController(TelegramClient client, RadioState state) {
        this.client = client;
        this.state = state;
    }

    // Spring --> Telegram. Тело запроса — просто строка (text/plain). */
    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> send(@RequestBody String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "empty", "message", "Строка не должна быть пустой"));
        }
        Optional<String> chatId = state.chatId();
        if (chatId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "status", "chat_not_found",
                    "message", "Сначала отправьте любое сообщение (например /start) своему боту в Telegram"));
        }
        try {
            client.sendMessage(chatId.get(), text);
            return ResponseEntity.ok(Map.of("status", "sent"));
        } catch (RestClientException e) {
            log.error("Не удалось отправить сообщение в Telegram", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("status", "telegram_error", "message", String.valueOf(e.getMessage())));
        }
    }

    // Сообщения, пришедшие из Telegram (для frontend). after — id последнего уже
    // полученного. */
    @GetMapping("/messages")
    public List<RadioState.ReceivedMessage> messages(@RequestParam(defaultValue = "0") long after) {
        return state.after(after);
    }

}

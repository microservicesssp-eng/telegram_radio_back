package com.example.telegram_radio.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.telegram_radio.config.TelegramProperties;

@Component
public class TelegramClient {

    private final TelegramProperties props;
    private final RestClient restClient;

    public TelegramClient(TelegramProperties props) {
        this.props = props;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(40));
        this.restClient = RestClient.builder()
                .baseUrl(props.apiUrl())
                .requestFactory(factory)
                .build();
    }

    public boolean isTokenConfigured() {

        String token = props.botToken();
        return token != null && !token.isBlank() && !token.startsWith("YOUR_");

    }

    public void sendMessage(String chatId, String text) {
        restClient.post()
                .uri("/bot{token}/sendMessage", props.botToken())
                .body(Map.of("chat_id", chatId, "text", text))
                .retrieve()
                .toBodilessEntity();
    }

    public List<Update> getUpdates(long offset, int timeoutSeconds) {
        Map<?, ?> response = restClient.get()
                .uri("/bot{token}/getUpdates?offset={offset}&timeout={timeout}",
                        props.botToken(), offset, timeoutSeconds)
                .retrieve()
                .body(Map.class);

        List<Update> updates = new ArrayList<>();
        Object result = response == null ? null : response.get("result");
        if (result instanceof List<?> list) {
            for (Object item : list) {

                if (!(item instanceof Map<?, ?> u)) {
                    continue;
                }
                long updateId = ((Number) u.get("update_id")).longValue();
                String chatId = null;
                String from = null;
                String text = null;
                if (u.get("message") instanceof Map<?, ?> m) {
                    if (m.get("chat") instanceof Map<?, ?> chat && chat.get("id") != null) {
                        chatId = String.valueOf(chat.get("id"));
                    }
                    if (m.get("from") instanceof Map<?, ?> f && f.get("first_name") != null) {
                        from = String.valueOf(f.get("first_name"));
                    }
                    if (m.get("text") != null) {
                        text = String.valueOf(m.get("text"));
                    }
                }
                updates.add(new Update(updateId, chatId, from, text));
            }
        }
        return updates;

    }

    public record Update(long updateId, String chatId, String from, String text) {

    }

}

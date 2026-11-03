package com.example.telegram_radio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class TelegramPoller {

    private static final Logger log = LoggerFactory.getLogger(TelegramPoller.class);

    private final TelegramClient client;
    private final RadioState state;

    private volatile boolean running;
    private Thread thread;

    public TelegramPoller(TelegramClient client, RadioState state) {
        this.client = client;
        this.state = state;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

        if (!client.isTokenConfigured()) {
            log.warn("Токен Telegram не задан (заглушка) - приём сщщбщений из Telegram отключен. "
                    + "Укажите telegram.bot-token или TELEGRAM_BOT_TOKEN и перезапустите.");
            return;

        }
        running = true;
        thread = new Thread(this::loop, "telegram-poller");
        thread.setDaemon(true);
        thread.start();
        log.info("Слушаю сщщбщения из Telegram.  Напишите что-нибудь своему боту.");

    }

    @PreDestroy
    public void stop() {

        running = false;
        if (thread != null) {
            thread.interrupt();
        }

    }

    private void loop() {
        long offset = 0;

        while (running) {
            try {
                for (TelegramClient.Update update : client.getUpdates(offset, 25)) {

                    offset = Math.max(offset, update.updateId() + 1);
                    if (update.chatId() != null) {
                        state.rememberChat(update.chatId());
                    }

                    if (update.text() != null) {
                        String from = update.from() != null ? update.from() : "unknown";
                        state.add(from, update.text());
                        System.out.println("[Telegram -> Spring] " + from + ": " + update.text());
                    }

                }
            } catch (Exception e) {
                if (!running) {
                    return;
                }
                log.warn("Ошибка getUdates: {} (повтор через 5с ). Если ошибка 409 - у бота "
                        + " включён webhook, отключите его (deleteWebhook).", e.getMessage());

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();

                    return;
                }
            }

        }
    }

}

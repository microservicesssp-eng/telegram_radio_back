package com.example.telegram_radio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.telegram_radio.config.TelegramProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramRadioApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramRadioApplication.class, args);
	}

}

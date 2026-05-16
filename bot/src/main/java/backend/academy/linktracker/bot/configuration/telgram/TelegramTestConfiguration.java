package backend.academy.linktracker.bot.configuration.telgram;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Тестовая конфигурация
 * чтобы избежать создания всех бинов используется
 * lazyInit
 */
@Configuration
@ComponentScan(basePackages = "backend.academy.linktracker.bot", lazyInit = true)
public class TelegramTestConfiguration {}

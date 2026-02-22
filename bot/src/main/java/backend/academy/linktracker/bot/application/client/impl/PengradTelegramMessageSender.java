package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PengradTelegramMessageSender implements TelegramMessageSender {

    private final TelegramBot telegramBot;

    @Autowired
    public PengradTelegramMessageSender(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
    }

}

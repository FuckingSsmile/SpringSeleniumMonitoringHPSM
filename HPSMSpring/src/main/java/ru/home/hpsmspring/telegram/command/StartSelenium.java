package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.telegram.TelegramBot;
import ru.home.hpsmspring.monitoring.SeleniumMonitoring;

import java.util.function.Consumer;

@Component
public class StartSelenium implements Consumer<Message> {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Autowired
    private SeleniumMonitoring seleniumMonitoring;

    @Override
    public void accept(Message message) {

        seleniumMonitoring.startSelenium();

        telegramBot.sendMessageText(message.getChatId().toString()
                ,"selenium запущен");

    }
}

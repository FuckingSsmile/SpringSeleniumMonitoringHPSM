package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.scheduler.RunMonitoringNotification;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.util.function.Consumer;

@Component
public class DisableNotification implements Consumer<Message> {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Autowired
    private RunMonitoringNotification runMonitoringNotification;

    @Override
    public void accept(Message message) {

        runMonitoringNotification.setStatusMonitoring(false);

        telegramBot.sendMessageText(message.getChatId().toString()
                ,"Остановлен планировщик уведомлений");

    }
}

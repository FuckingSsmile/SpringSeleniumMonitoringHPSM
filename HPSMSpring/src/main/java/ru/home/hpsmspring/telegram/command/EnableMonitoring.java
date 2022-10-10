package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.scheduler.RunCheckHPSM;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.util.function.Consumer;

@Component
public class EnableMonitoring implements Consumer<Message> {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Autowired
    private RunCheckHPSM runCheckHPSM;

    @Override
    public void accept(Message message) {

        runCheckHPSM.setStatusMonitoring(true);

        telegramBot.sendMessageText(message.getChatId().toString()
                ,"Запущен мониторинг заявок");

    }
}

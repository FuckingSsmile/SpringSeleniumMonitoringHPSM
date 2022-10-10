package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.util.function.Consumer;

@Component
public class GetChatId implements Consumer<Message> {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Override
    public void accept(Message message) {

        telegramBot.sendMessageText(message.getChatId().toString()
                , "ID chat:" + message.getChatId() + "\n"
                        + "User ID:" + message.getFrom().getId());

    }
}
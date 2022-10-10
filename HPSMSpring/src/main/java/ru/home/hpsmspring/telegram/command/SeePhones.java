package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.phone.PhoneBook;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.util.function.Consumer;

@Component
public class SeePhones implements Consumer<Message> {

    @Autowired
    private PhoneBook phoneBook;

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Override
    public void accept(Message message) {

        if (!phoneBook.getAllPhone().isEmpty()) {

            telegramBot.sendMessageText(message.getChatId().toString(),phoneBook.getAllPhone().toString());

    } else {
            telegramBot.sendMessageText(message.getChatId().toString(),"Телефонный список - пуст");

        }
    }
}

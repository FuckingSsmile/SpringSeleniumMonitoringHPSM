package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.phone.PhoneBook;
import ru.home.hpsmspring.telegram.TelegramBot;
import ru.home.hpsmspring.telegram.state.TelegramBotState;

import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class RemovePhones implements Consumer<Message> {

    @Autowired
    private TelegramBotState botState;

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Autowired
    private PhoneBook phoneBook;

    @Override
    public void accept(Message message) {

        botState.setUserIdForState(message.getFrom().getId());

        String command = message.getText().replace(telegramBot.getBotUsername(), "").trim();

        botState.changeActivityState(command);

        telegramBot.createAndSendInlineKeyboard(message.getChatId().toString()
        ,new ArrayList<>(phoneBook.getAllPhone()), "Выберите номер для удаления");

    }
}

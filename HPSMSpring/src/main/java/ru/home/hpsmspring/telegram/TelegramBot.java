package ru.home.hpsmspring.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.home.hpsmspring.monitoring.SeleniumMonitoring;
import ru.home.hpsmspring.phone.PhoneBook;
import ru.home.hpsmspring.telegram.command.*;
import ru.home.hpsmspring.telegram.state.TelegramBotState;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private HashMap<String, Consumer<Message>> commandMap = new HashMap<>();

    //Чат
    @Value("${chat.prod}")
    private String prodChat;

    @Autowired
    private TelegramBotState botState;

    //Бот
    @Value("${bot.userName}")
    private String userName;
    @Value("${bot.token}")
    private String token;

    @Autowired
    private SeleniumMonitoring seleniumMonitoring;

    @Autowired
    private PhoneBook phoneBook;

    private final double columns = 2;

    @Autowired
    public TelegramBot(GetTasks getTasks
            , DisableNotification disableNotification
            , TestCommand testCommand
            , StartSelenium startSelenium
            , EndSelenium endSelenium
            , EnableMonitoring enableMonitoring
            , EnableNotification enableNotification
            , GetChatId getChatId
            , DisableMonitoring disableMonitoring
            , AddPhone addPhone
            , SeePhones seePhones
            , RemovePhones removePhone) {
        commandMap.put("/gettasks", getTasks);
        commandMap.put("/disablenotification", disableNotification);
        commandMap.put("/startselenium", startSelenium);
        commandMap.put("/endselenium", endSelenium);
        commandMap.put("/enablemonitoring", enableMonitoring);
        commandMap.put("/testcommand", testCommand);
        commandMap.put("/enablenotification", enableNotification);
        commandMap.put("/disablemonitoring", disableMonitoring);
        commandMap.put("/getchatid", getChatId);
        commandMap.put("/addphone", addPhone);
        commandMap.put("/removephone", removePhone);
        commandMap.put("/seephones", seePhones);
    }

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (update.hasMessage()) {

            if (message.hasText()) {

                if (message.hasText() & message.getChatId().toString().equals(prodChat)) {

                    String textHasMessage = message.getText().replace(getBotUsername(), "");
                    String incomingChatId = message.getChatId().toString();

                    if (botState.getUserIdForState() == message.getFrom().getId()) {

                        switch (botState.getState()) {

                            case WaitingNewPhone:
                                if (textHasMessage.matches("\\+7\\d{10}") && !phoneBook.getAllPhone().contains(textHasMessage)) {

                                    phoneBook.addPhoneNumber(textHasMessage);

                                    sendMessageText(incomingChatId
                                            , textHasMessage + " - успешно добавлен");

                                    sendMessageText(incomingChatId
                                            , phoneBook.getAllPhone().toString());
                                } else {
                                    sendMessageText(incomingChatId
                                            , "Неверный формат или такой номер уже существует.\nВведите команду заново");
                                }

                                botState.stateFree();
                                break;

//                            case WaitingRemovePhone:
//                                if (phoneBook.getAllPhone().contains(textHasMessage)) {
//
//                                    phoneBook.removePhoneNumber(textHasMessage);
//
//                                    sendMessageText(incomingChatId
//                                            , textHasMessage + " - успешно удален");
//                                } else {
//
//                                    sendMessageText(incomingChatId
//                                            , "Неверный формат или такой номер уже существует.\nВведите команду заново");
//
//                                }
//
//                                botState.stateFree();
//                                break;
                        }
                    }


                    Consumer<Message> messageConsumer = commandMap.get(textHasMessage);

                    if (messageConsumer != null) {

                        messageConsumer.accept(message);
                    }
                }
            }
        }

        if (update.hasCallbackQuery()) {

            Message messageCallBack = update.getCallbackQuery().getMessage();
            String chatIdCallBack = messageCallBack.getChatId().toString();
            Integer messageId = messageCallBack.getMessageId();

            String answerData = update.getCallbackQuery().getData();

            if (answerData.matches("Возьмет в работу " + "\\w[IM|RF]\\d{0,9}")){

                String user = update.getCallbackQuery().getFrom().getUserName() != null
                        ? update.getCallbackQuery().getFrom().getUserName()
                        : update.getCallbackQuery().getFrom().getFirstName();

                sendMessageText(chatIdCallBack, "@"+user + " " +update.getCallbackQuery().getData());

                editMessage(chatIdCallBack, messageId, messageCallBack.getText());
                return;

            }




            if (botState.getUserIdForState() == update.getCallbackQuery().getFrom().getId()) {
                System.out.println(botState.getUserIdForState() + "  "  + messageCallBack.getFrom().getId());

                switch (botState.getState()) {

                    case WaitingRemovePhone:
                        if (phoneBook.getAllPhone().contains(answerData)) {

                            phoneBook.removePhoneNumber(answerData);

                            sendMessageText(chatIdCallBack
                                    , answerData + " - успешно удален");
                        } else {

                            sendMessageText(chatIdCallBack
                                    , "Неверный формат или такой номер уже существует.\nВведите команду заново");

                        }

                        botState.stateFree();
                        break;
                }
            }


            //Процесс взятия в работу заявки
            if (answerData.matches("\\w[IM|RF]\\d{0,9}")) {

                editMessage(chatIdCallBack, messageId, "Берем в работу - " + answerData);

                seleniumMonitoring.workWithHpsm(answerData);

            }
        }
    }

    @Override
    public String getBotUsername() {
        return userName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public void sendMessageText(String chatId, String text) {
            try {

                execute(SendMessage.builder().parseMode("HTML").chatId(chatId).text(text).build());

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    }

    public void sendMessageTextAndButton(String chatId, String text, String taskId,String textForAnswer) {
                createAndSendInlineKeyboard(chatId, text,taskId, textForAnswer);

    }

    public void editMessage(String chatId, Integer messageId, String text) {

        try {
            execute(EditMessageText
                    .builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(text)
                    .build()
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void createAndSendInlineKeyboard(String chatId, List<String> ids, String textForKeyboards) {

        List<InlineKeyboardButton> buttons = ids.stream()
                .map(text -> InlineKeyboardButton.builder()
                        .text(text)
                        .callbackData(text)
                        .build())
                .collect(Collectors.toList());

        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(buttons);

        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(textForKeyboards)
                    .replyMarkup(inlineKeyboardMarkup)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void createAndSendInlineKeyboard(String chatId, String ids, String textForKeyboards, String textForAnswer) {

        List<InlineKeyboardButton> buttons = List.of(InlineKeyboardButton.builder()
                .text(textForKeyboards)
                .callbackData(textForAnswer + textForKeyboards)
                .build());

        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(buttons);

        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(ids)
                    .parseMode("HTML")
                    .replyMarkup(inlineKeyboardMarkup)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public InlineKeyboardMarkup createInlineKeyboardButton(List<String> textList) {

        List<InlineKeyboardButton> buttons = textList.stream()
                .map(text -> InlineKeyboardButton.builder()
                        .text(text)
                        .callbackData(text)
                        .build())
                .collect(Collectors.toList());

        return createInlineKeyboard(buttons);

    }

    public InlineKeyboardMarkup createInlineKeyboard(List<InlineKeyboardButton> buttons) {

        double rowsCount = Math.ceil(buttons.size() / columns);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Iterator<InlineKeyboardButton> iterator = buttons.iterator();

        for (int i = 0; i < rowsCount; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            rows.add(row);
            for (int j = 0; j < columns & iterator.hasNext(); j++) {
                row.add(iterator.next());
            }
        }
        List<List<InlineKeyboardButton>> keyboards = rows;

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboards);

        return inlineKeyboardMarkup;
    }
}

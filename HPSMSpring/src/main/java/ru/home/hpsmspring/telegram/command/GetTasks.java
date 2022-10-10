package ru.home.hpsmspring.telegram.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.home.hpsmspring.hpsm.HPSM;
import ru.home.hpsmspring.hpsm.tickets.TaskCollections;
import ru.home.hpsmspring.monitoring.SeleniumMonitoring;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class GetTasks implements Consumer<Message> {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Autowired
    private SeleniumMonitoring seleniumMonitoring;

    @Override
    public void accept(Message message) {

        seleniumMonitoring.workWithHpsm();

        List<String> newTask = new ArrayList<>();

        StringBuilder text = new StringBuilder();

        if (TaskCollections.currentTasks.size() != 0) {

            text.append("<b>Актуальные задачи</b>\n\n");

            for (Map.Entry<String, HPSM> stringHPSMEntry : TaskCollections.currentTasks.entrySet()) {

                if ((stringHPSMEntry.getValue().getStatus().equalsIgnoreCase("Назначен")
                        || stringHPSMEntry.getValue().getStatus().equalsIgnoreCase("Назначено"))
                        && !stringHPSMEntry.getValue().getWorkingGroup().equalsIgnoreCase("РГ ЕМИАС.МГФОМС")) {
                    System.out.println("Положили таску во взятие в работу");
                    newTask.add(stringHPSMEntry.getKey());
                }
                        text.append("<i>")
                        .append(stringHPSMEntry.getValue().getTaskId())
                        .append("</i>\n")
                        .append("<i>")
                        .append(stringHPSMEntry.getValue().getWorkingGroup())
                        .append("</i>\n")
                        .append("<i>")
                        .append(stringHPSMEntry.getValue().getStatus())
                        .append("</i>\n\n");
            }
        } else {
            text.append("Актуальных задач нет");
        }


        telegramBot.sendMessageText(message.getChatId().toString()
                , text.toString());


        if (newTask.size() != 0) {
            telegramBot.createAndSendInlineKeyboard(message.getChatId().toString()
                    , newTask
                    , "Задачи которые можно взять в работу");
        }
    }
}



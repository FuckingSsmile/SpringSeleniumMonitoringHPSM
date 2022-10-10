package ru.home.hpsmspring.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.hpsm.HPSM;
import ru.home.hpsmspring.hpsm.sms.SmsPackage;
import ru.home.hpsmspring.hpsm.tickets.TaskCollections;
import ru.home.hpsmspring.telegram.TelegramBot;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class MonitoringEventSmsCall {

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    public LinkedList<SmsPackage> smsPackages = new LinkedList<>();

    private List<String> awaitingTasks = new ArrayList<>();
    private List<String> awaitingTasksForSms = new ArrayList<>();
    private List<String> awaitingTasksForCall = new ArrayList<>();

    private final int timeForSendingToTelegram = 10;
    private final int timeForSendingToSms = 15;
    private final int timeForCall = 20;


    //интервал работы уведомлений на телефон
    private LocalTime starthours = LocalTime.of(8, 0);
    private LocalTime endhours = LocalTime.of(22, 0);

    //запустить планировщик
    public void runMonitoringNotification(String chatId) {

        LocalDateTime now = LocalDateTime.now();

        if (!TaskCollections.currentTasks.isEmpty()) {
            //Проверка по времени
            for (Map.Entry<String, HPSM> entry : TaskCollections.currentTasks.entrySet()) {

                if (entry.getValue().getStatus().equalsIgnoreCase("Назначен")
                && !entry.getValue().getWorkingGroup().equalsIgnoreCase("РГ ЕМИАС.МГФОМС")) {


                    long minutes = ChronoUnit.MINUTES.between(entry.getValue().getLocalDateTime(), now);
                    //Заявка в ожидании для отправки в чат (10 минут)
                    if (minutes == timeForSendingToTelegram && entry.getValue().isSendTelegram()) {
                        awaitingTasks.add(entry.getValue().getTaskId());
                        entry.getValue().setSendTelegram(false);
                        continue;
                    }
                    //Заявка в ожидании для отправки смс (15мин)
                    if (minutes == timeForSendingToSms && entry.getValue().isSendSms()) {
                        awaitingTasksForSms.add(entry.getValue().getTaskId());
                        entry.getValue().setSendSms(false);
                        continue;
                    }
                    //Заявка в ожидании для звонка (20мин)
                    if (minutes == timeForCall && entry.getValue().isSendCall()) {

                        if (LocalTime.now().isAfter(starthours) && LocalTime.now().isBefore(endhours)) {
                            awaitingTasksForCall.add(entry.getValue().getTaskId());
                            entry.getValue().setSendCall(false);

                        } else {
                            telegramBot.sendMessageText(chatId
                                    , "Больше 20 минут в статусе назначен\n Нерабочее время для обзвона\n\n" + awaitingTasksForCall);
                        }
                    }
                }
            }


            //происходит проверка массива и рассылка
            if (!awaitingTasks.isEmpty()) {
                telegramBot.sendMessageText(chatId
                        , "Больше 10 минут в статусе назначен\n\n" + awaitingTasks);

                awaitingTasks.clear();
            }

            if (!awaitingTasksForSms.isEmpty()) {
                telegramBot.sendMessageText(chatId
                        , "Больше 15 минут в статусе назначен\nПроизведена рассылка по СМС\n\n" + awaitingTasksForSms);

                smsPackages.add(new SmsPackage("Больше 15 минут в статусе назначен "
                        + awaitingTasksForSms
                        , 0
                        , ""));

                awaitingTasksForSms.clear();
            }

            if (!awaitingTasksForCall.isEmpty()) {
                telegramBot.sendMessageText(chatId
                        , "Больше 20 минут в статусе назначен\nПроизведен обзвон\n\n" + awaitingTasksForCall);

                smsPackages.add(new SmsPackage("Есть задачи, более 20 минут в статусе назначен, номер "
                        + awaitingTasksForCall
                        , 9
                        , "voice=w2&param=10,10,1"));

                awaitingTasksForCall.clear();
            }
        }

    }
}
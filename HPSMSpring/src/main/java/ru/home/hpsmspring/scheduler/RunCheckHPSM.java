package ru.home.hpsmspring.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.hpsm.HPSM;
import ru.home.hpsmspring.hpsm.tickets.TaskCollections;
import ru.home.hpsmspring.telegram.TelegramBot;
import ru.home.hpsmspring.monitoring.SeleniumMonitoring;

import java.util.List;

@Component
public class RunCheckHPSM {

    @Autowired
    private SeleniumMonitoring seleniumMonitoring;

    @Autowired
    @Lazy
    private TelegramBot telegramBot;

    @Value("${chat.prod}")
    private String chatId;

    private boolean statusMonitoring = false;

    @Scheduled(initialDelayString = "${schedule.checkHPSM.init}", fixedRateString = "${schedule.checkHPSM.work}")
    public void checkTickets() {
        if (statusMonitoring) {

            seleniumMonitoring.workWithHpsm();

            if (TaskCollections.updateCurrentTasks()) {

                sendToTelegram(TaskCollections.getListObjectWithUpdate());
            }

        }
    }

    private void sendToTelegram(List<HPSM> hpsmList) {

        for (int i = 0; i < hpsmList.size(); i++) {
            HPSM hpsm = hpsmList.get(i);
            if (!hpsm.getUpdateDescription().isEmpty()) {

                if (hpsm.getStatus().equals("Назначен") || hpsm.getStatus().equals("Назначено")) {

                    telegramBot.sendMessageTextAndButton(chatId, hpsm.getUpdateDescription(), hpsm.getTaskId(), "Возьмет в работу ");

                } else {

                    telegramBot.sendMessageText(chatId, hpsm.getUpdateDescription());
                }

//                if(hpsm.isClosedTask()){
//                    TaskCollections.currentTasks.remove(hpsm.getTaskId());
//                }
                hpsm.setUpdateDescription("");
            }

        }
    }

    public void setStatusMonitoring(boolean statusMonitoring) {
        this.statusMonitoring = statusMonitoring;
    }
}
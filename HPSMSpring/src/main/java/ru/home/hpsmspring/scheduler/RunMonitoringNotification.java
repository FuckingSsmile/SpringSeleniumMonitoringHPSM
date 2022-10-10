package ru.home.hpsmspring.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.hpsm.sms.Smsc;
import ru.home.hpsmspring.monitoring.MonitoringEventSmsCall;
import ru.home.hpsmspring.phone.PhoneBook;

@Component
public class RunMonitoringNotification {

    @Value("${chat.prod}")
    private String chatId;

    @Autowired
    private Smsc smsc;

    @Autowired
    private MonitoringEventSmsCall monitoringEventSmsCall;

    @Autowired
    private PhoneBook phoneBook;

    private boolean statusMonitoring = false;

    @Scheduled(initialDelayString = "${schedule.monitoring.init}", fixedRateString = "${schedule.monitoring.work}")
    public void checkEvents() {

        if (statusMonitoring) {

            monitoringEventSmsCall.runMonitoringNotification(chatId);

            smsc.runCheckEventForPhone(phoneBook.getAllPhone());

        }
    }

    public void setStatusMonitoring(boolean statusMonitoring) {
        this.statusMonitoring = statusMonitoring;
    }
}

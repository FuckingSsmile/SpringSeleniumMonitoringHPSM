package ru.home.hpsmspring.scheduler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.phone.PhoneBook;

@Component
public class RunSavePhoneBook {

    @Autowired
    private PhoneBook phoneBook;

    @Scheduled(initialDelayString = "${schedule.phoneBook.init}", fixedRateString = "${schedule.phoneBook.work}")
    public void savePhoneBooks(){

        phoneBook.savePhones();

    }
}

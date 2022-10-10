package ru.home.hpsmspring.phone;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PhoneBook {

    private Set<String> allPhones;
    private String separator = File.separator;

    private String pathPhones = System.getProperty("user.dir") + separator + "phones";


    public PhoneBook() {

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(pathPhones));
            allPhones = (Set<String>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            allPhones = new CopyOnWriteArraySet<>();
        }
    }

    public void savePhones() {

        try {
            Set<String> finalAllPhones = allPhones;
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(pathPhones));

            objectOutputStream.writeObject(finalAllPhones);
            objectOutputStream.flush();
            objectOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPhoneNumber(String phoneNumber) {
        allPhones.add(phoneNumber);
    }

    public void removePhoneNumber(String phoneNumber){
        allPhones.remove(phoneNumber);
    }

    public Set<String> getAllPhone() {
        return allPhones;
    }

}

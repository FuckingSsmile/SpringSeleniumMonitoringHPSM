package ru.home.hpsmspring.hpsm.tickets;

import ru.home.hpsmspring.hpsm.HPSM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskCollections {
    public static ConcurrentHashMap<String, HPSM> currentTasks = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, HPSM> newTasks = new ConcurrentHashMap<>();

    private static boolean check = false;
    private static DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


    public static boolean updateCurrentTasks() {

        check = false;

        if (newTasks.size() == 0) {
            return false;
        }

        //Первый запуск, просто добавляеям все заявки как новые
        if (currentTasks.size() == 0) {

            return firstStart();

        }

        //Поиск новых заявок. Сравниваем две мапы
        checkNewTicket();

        //Поиск выполненых или закрытых задач
        checkClosedTicket();

        //поиск задач, где изменился статус или рабочая группа

        equalsHPSM();

        TaskCollections.newTasks.clear();

        return check;
    }

    //Сравниваем статус заявки, на предмет изменений
    private static void equalsHPSM() {

        for (Map.Entry<String, HPSM> newHPSMEntry : newTasks.entrySet()) {

            HPSM currentHPSM = currentTasks.get(newHPSMEntry.getKey());
            HPSM newHPSM = newHPSMEntry.getValue();


            if (!currentHPSM.getStatus().equals(newHPSM.getStatus())) {

                System.out.println("Изменилась заявка");

                currentHPSM.setUpdateDescription("✏️✏️✏️\n"
                        + LocalDateTime.now().format(formatTime)
                        + "\n\n<b>Изменился статус у заявки - " + currentHPSM.getTaskId() + "</b>"
                        + "\n\n<i>Был</i>\n"
                        + "<b>"+currentHPSM.getStatus()+"</b>"
                        + "\n\n<i>Стал</i>\n"
                        + "<b>"+newHPSM.getStatus()+"</b>");

                currentHPSM.setStatus(newHPSM.getStatus());
                currentHPSM.setWorkingGroup(newHPSM.getWorkingGroup());

                check = true;

            }
        }
    }

    public static List<HPSM> getListObjectWithUpdate() {
        return new ArrayList<>(currentTasks.values());
    }

    //Первый запуск программы, все задачи идут как новые
    private static boolean firstStart() {

        System.out.println("Первый запуск, добавляем все задачи");
        for (Map.Entry<String, HPSM> newHPSMEntry : newTasks.entrySet()) {

            newHPSMEntry.getValue().setUpdateDescription("‼️‼️‼️\n"
                    + LocalDateTime.now().format(formatTime)
                    + "\n\n<b>Пришла новая задача</b>\n\n"
                    + newHPSMEntry.getValue());
        }
        currentTasks.putAll(newTasks);

        return true;
    }

    //Проверяем на наличие новых задач
    private static void checkNewTicket() {

        for (Map.Entry<String, HPSM> newHPSMEntry : newTasks.entrySet()) {

            if (!currentTasks.containsKey(newHPSMEntry.getKey())) {

                System.out.println("Новая задача - " + newHPSMEntry.getKey());

                newHPSMEntry.getValue().setUpdateDescription("‼️‼️‼️\n"
                        + LocalDateTime.now().format(formatTime)
                        + "\n\n<b>Пришла новая задача</b>\n\n"
                        + newHPSMEntry.getValue());

                currentTasks.put(newHPSMEntry.getKey(), newHPSMEntry.getValue());
                check = true;
            }
        }
    }

    //поиск закрытых или выполненых задач
    private static void checkClosedTicket() {

        for (Map.Entry<String, HPSM> currentHPSMEntry : currentTasks.entrySet()) {
            //TODO новый код
            if(newTasks.isEmpty()){
                return;
            }

            if (!newTasks.containsKey(currentHPSMEntry.getKey())) {
                System.out.println("Задача выполнена");

//                currentHPSMEntry.getValue().setUpdateDescription("✅✅✅\n"
//                        + LocalDateTime.now().format(formatTime)
//                        + "\n\n<b>Задача выполнена</b>\n\n"
//                        + "<i>" + currentHPSMEntry.getValue().getTaskId() + "</i>"
//                        + "\n"
//                        + "<i>" + currentHPSMEntry.getValue().getWorkingGroup() + "</i>");
//
//                currentHPSMEntry.getValue().setClosedTask(true);

                currentTasks.remove(currentHPSMEntry.getKey());

                check = true;

            }
        }
    }
}

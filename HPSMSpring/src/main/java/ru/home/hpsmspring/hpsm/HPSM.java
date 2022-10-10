package ru.home.hpsmspring.hpsm;

import lombok.Getter;
import lombok.Setter;
import ru.home.hpsmspring.hpsm.tickets.TaskCollections;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class HPSM {

    private String taskId;
    private String workingGroup;
    private String status;
    private LocalDateTime localDateTime;
    private String updateDescription = "";
    private boolean isClosedTask = false;
    private boolean isSendTelegram = true;
    private boolean isSendSms = true;
    private boolean isSendCall = true;

    public HPSM(String taskId, String workingGroup, String status, LocalDateTime localDateTime) {
        this.taskId = taskId;
        this.workingGroup = workingGroup;
        this.status = status;
        this.localDateTime = localDateTime;
        TaskCollections.newTasks.put(taskId,this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HPSM hpsm = (HPSM) o;
        return taskId.equals(hpsm.taskId) && workingGroup.equals(hpsm.workingGroup) && status.equals(hpsm.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, workingGroup, status);
    }

    @Override
    public String toString() {
        return "<b>Номер заявки</b>: <i>" + taskId + "</i>\n" +
                "<b>Рабочая группа</b>: <i>" + workingGroup + "</i>\n" +
                "<b>Статус</b>: <i>" + status + "</i>\n";
    }
}

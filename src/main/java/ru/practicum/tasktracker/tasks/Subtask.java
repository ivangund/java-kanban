package main.java.ru.practicum.tasktracker.tasks;

import main.java.ru.practicum.tasktracker.enums.Status;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        if (id == epicId) {
            throw new IllegalArgumentException("Подзадача не может быть своим же эпиком");
        }
        this.epicId = epicId;
    }

    public Subtask(String title, String description, Duration duration,
            LocalDateTime startTime, int epicId) {
        super(title, description, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, Status status, Duration duration,
            LocalDateTime startTime, int epicId) {
        super(id, title, description, status, duration, startTime);
        if (id == epicId) {
            throw new IllegalArgumentException("Подзадача не может быть своим же эпиком");
        }
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + getDuration().toMinutes() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                '}';
    }
}

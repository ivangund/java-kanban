package main.java.ru.practicum.tasktracker.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import main.java.ru.practicum.tasktracker.tasks.*;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.enums.TaskType;
import main.java.ru.practicum.tasktracker.exceptions.ManagerLoadException;
import main.java.ru.practicum.tasktracker.exceptions.ManagerSaveException;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");
            for (Task task : getTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи в файл: " + e.getMessage());
        }
    }

    private String toString(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String startTimeStr =
                task.getStartTime() != null ? task.getStartTime().format(formatter) : "null";
        String durationStr =
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes())
                        : "null";

        if (task instanceof Subtask subtask) {
            return String.format("%d,SUBTASK,%s,%s,%s,%s,%s,%d", subtask.getId(),
                    subtask.getTitle(),
                    subtask.getStatus(), subtask.getDescription(), durationStr, startTimeStr,
                    subtask.getEpicId());
        } else if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,%s,%s,", task.getId(), task.getTitle(),
                    task.getStatus(), task.getDescription(), durationStr, startTimeStr);
        } else {
            return String.format("%d,TASK,%s,%s,%s,%s,%s,", task.getId(), task.getTitle(),
                    task.getStatus(), task.getDescription(), durationStr, startTimeStr);
        }
    }

    private Task fromString(String value) throws IllegalArgumentException {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        Duration duration =
                "null".equals(fields[5]) ? null : Duration.ofMinutes(Long.parseLong(fields[5]));
        LocalDateTime startTime = "null".equals(fields[6]) ? null : LocalDateTime.parse(fields[6]);

        return switch (type) {
            case TASK -> new Task(id, name, description, status, duration, startTime);
            case EPIC -> new Epic(id, name, description, status);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[7]);
                yield new Subtask(id, name, description, status, duration, startTime, epicId);
            }
        };
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                Task task = manager.fromString(lines.get(i));
                if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.createSubtask((Subtask) task);
                } else {
                    manager.createTask(task);
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            throw new ManagerLoadException(
                    "Не удалось загрузить задачи из файла: " + e.getMessage());
        }
        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public boolean deleteTask(int id) {
        boolean result = super.deleteTask(id);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public boolean deleteEpic(int id) {
        boolean result = super.deleteEpic(id);
        save();
        return result;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public boolean deleteSubtask(int id) {
        boolean result = super.deleteSubtask(id);
        save();
        return result;
    }
}

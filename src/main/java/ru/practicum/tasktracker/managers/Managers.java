package main.java.ru.practicum.tasktracker.managers;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBackedTaskManager() {
        File file = new File("data.csv");
        if (file.exists()) {
            return FileBackedTaskManager.loadFromFile(file);
        } else {
            return new FileBackedTaskManager(file);
        }
    }
}

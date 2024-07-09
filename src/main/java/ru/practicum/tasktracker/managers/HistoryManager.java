package main.java.ru.practicum.tasktracker.managers;

import java.util.List;
import main.java.ru.practicum.tasktracker.tasks.Task;

public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}

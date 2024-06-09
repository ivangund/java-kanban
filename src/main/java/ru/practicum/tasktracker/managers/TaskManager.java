package main.java.ru.practicum.tasktracker.managers;

import java.util.List;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import main.java.ru.practicum.tasktracker.tasks.Task;

public interface TaskManager {

    List<Task> getTasks();

    void deleteTasks();

    Task getTask(int id);

    Task createTask(Task task);

    Task updateTask(Task task);

    boolean deleteTask(int id);

    List<Epic> getEpics();

    Epic getEpic(int id);

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    boolean deleteEpic(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    List<Subtask> getSubtasks();

    Subtask getSubtask(int id);

    Subtask createSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    boolean deleteSubtask(int id);

    List<Task> getHistory();
}

package test;

import java.util.List;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import main.java.ru.practicum.tasktracker.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HistoryManagerTests {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testAddTaskToHistory() {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача в истории должна быть task2.");
    }

    @Test
    void testRemoveTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());

        taskManager.deleteTask(task1.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size(),
                "История должна содержать одну задачу после удаления task1.");
        assertEquals(task2, history.getFirst(),
                "Единственная оставшаяся задача в истории должна быть task2.");
    }

    @Test
    void testHistoryManagerRetainsPreviousTaskVersion() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        taskManager.getTask(task.getId());

        Task updatedTask = new Task(task.getId(), "Обновленная задача", "Обновленное описание",
                Status.IN_PROGRESS);
        taskManager.updateTask(updatedTask);

        List<Task> history = taskManager.getHistory();

        Task oldTask = history.getFirst();
        assertNotEquals(updatedTask.getTitle(), oldTask.getTitle(),
                "В истории должна сохраниться старая версия задачи.");
        assertNotEquals(updatedTask.getDescription(), oldTask.getDescription(),
                "В истории должна сохраниться старая версия задачи.");
        assertNotEquals(updatedTask.getStatus(), oldTask.getStatus(),
                "В истории должна сохраниться старая версия задачи.");
    }

    @Test
    void testHistoryManagerRetainsPreviousSubtaskVersion() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.getSubtask(subtask.getId());

        Subtask updatedSubtask = new Subtask(subtask.getId(), "Обновленная подзадача",
                "Обновленное описание", Status.IN_PROGRESS, epic.getId());
        taskManager.updateSubtask(updatedSubtask);

        List<Task> history = taskManager.getHistory();

        Subtask oldSubtask = (Subtask) history.getFirst();
        assertNotEquals(updatedSubtask.getTitle(), oldSubtask.getTitle(),
                "В истории должна сохраниться старая версия подзадачи.");
        assertNotEquals(updatedSubtask.getDescription(), oldSubtask.getDescription(),
                "В истории должна сохраниться старая версия подзадачи.");
        assertNotEquals(updatedSubtask.getStatus(), oldSubtask.getStatus(),
                "В истории должна сохраниться старая версия подзадачи.");
    }

    @Test
    void testHistoryManagerRetainsPreviousEpicVersion() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        taskManager.getEpic(epic.getId());

        Epic updatedEpic = new Epic(epic.getId(), "Обновленный эпик", "Обновленное описание",
                Status.IN_PROGRESS);
        taskManager.updateEpic(updatedEpic);

        List<Task> history = taskManager.getHistory();

        Epic oldEpic = (Epic) history.getFirst();
        assertNotEquals(updatedEpic.getTitle(), oldEpic.getTitle(),
                "В истории должна сохраниться старая версия эпика.");
        assertNotEquals(updatedEpic.getDescription(), oldEpic.getDescription(),
                "В истории должна сохраниться старая версия эпика.");
        assertNotEquals(updatedEpic.getStatus(), oldEpic.getStatus(),
                "В истории должна сохраниться старая версия эпика.");
    }
}

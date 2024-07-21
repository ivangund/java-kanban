package test.java.ru.practicum.tasktracker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.managers.FileBackedTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTests extends InMemoryTaskManagerTests {

    private File tempFile;

    @BeforeEach
    void setUp() {
        try {
            tempFile = File.createTempFile("tasktracker", ".csv");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл: " + e.getMessage());
        }
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым.");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым.");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым.");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getTasks();
        assertEquals(2, tasks.size(), "Должно быть две задачи.");
        assertTrue(tasks.contains(task1), "Должна присутствовать задача 1.");
        assertTrue(tasks.contains(task2), "Должна присутствовать задача 2.");
    }

    @Test
    void testSaveAndLoadMultipleEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Epic> epics = loadedManager.getEpics();
        assertEquals(2, epics.size(), "Должно быть два эпика.");
        assertTrue(epics.contains(epic1), "Должен присутствовать эпик 1.");
        assertTrue(epics.contains(epic2), "Должен присутствовать эпик 2.");
    }

    @Test
    void testSaveAndLoadMultipleSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Subtask> subtasks = loadedManager.getSubtasks();
        assertEquals(2, subtasks.size(), "Должно быть две подзадачи.");
        assertTrue(subtasks.contains(subtask1), "Должна присутствовать подзадача 1.");
        assertTrue(subtasks.contains(subtask2), "Должна присутствовать подзадача 2.");
    }
}

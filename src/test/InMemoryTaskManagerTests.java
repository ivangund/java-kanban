package test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.exceptions.OverlapException;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTests {

    protected TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testAddTask() {
        Task task = new Task("Задача", "Описание");
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask, "Созданная задача не должна быть null.");
        assertEquals(task, createdTask, "Созданная задача должна совпадать с исходной.");
    }

    @Test
    void testRetrieveTask() {
        Task task = new Task("Задача", "Описание");
        Task createdTask = taskManager.createTask(task);

        Task retrievedTask = taskManager.getTask(createdTask.getId());

        assertNotNull(retrievedTask, "Извлеченная задача не должна быть null.");
        assertEquals(createdTask, retrievedTask,
                "Созданная и извлеченная задачи должны быть равны.");
    }

    @Test
    void testAddEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic, "Созданный эпик не должен быть null.");
        assertEquals(epic, createdEpic, "Созданный эпик должен совпадать с исходным.");
    }

    @Test
    void testRetrieveEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        Epic createdEpic = taskManager.createEpic(epic);

        Epic retrievedEpic = taskManager.getEpic(createdEpic.getId());

        assertNotNull(retrievedEpic, "Извлеченный эпик не должен быть null.");
        assertEquals(createdEpic, retrievedEpic,
                "Созданный и извлеченный эпики должны быть равны.");
    }

    @Test
    void testAddSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask, "Созданная подзадача не должна быть null.");
        assertEquals(subtask, createdSubtask, "Созданная подзадача должна совпадать с исходной.");
    }

    @Test
    void testRetrieveSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        Subtask retrievedSubtask = taskManager.getSubtask(createdSubtask.getId());

        assertNotNull(retrievedSubtask, "Извлеченная подзадача не должна быть null.");
        assertEquals(createdSubtask, retrievedSubtask,
                "Созданная и извлеченная подзадачи должны быть равны.");
    }

    @Test
    void testTaskIdConflict() {
        Task taskWithGeneratedId = new Task("Задача ID сгенерирован", "Описание");
        Task taskWithCustomId = new Task(taskWithGeneratedId.getId(), "Задача ID задан",
                "Описание", Status.NEW);

        taskManager.createTask(taskWithGeneratedId);
        taskManager.createTask(taskWithCustomId);

        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size(), "Должна быть одна задача.");
    }

    @Test
    void testSubtaskIdConflict() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtaskWithGeneratedId = new Subtask("Подзадача ID сгенерирован", "Описание",
                epic.getId());
        Subtask subtaskWithCustomId = new Subtask(subtaskWithGeneratedId.getId(),
                "Подзадача ID задан", "Описание", Status.NEW, epic.getId());

        taskManager.createSubtask(subtaskWithGeneratedId);
        taskManager.createSubtask(subtaskWithCustomId);

        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size(), "Должна быть одна подзадача.");
    }

    @Test
    void testEpicIdConflict() {
        Epic epicWithGeneratedId = new Epic("Эпик ID сгенерирован", "Описание");
        Epic epicWithCustomId = new Epic(epicWithGeneratedId.getId(), "Эпик ID задан",
                "Описание", Status.NEW);

        taskManager.createEpic(epicWithGeneratedId);
        taskManager.createEpic(epicWithCustomId);

        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Должен быть один эпик.");
    }

    @Test
    void testTaskOnAddition() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);

        Task retrievedTask = taskManager.getTask(task.getId());
        assertEquals(task.getTitle(), retrievedTask.getTitle(),
                "Название задачи не должно изменяться.");
        assertEquals(task.getDescription(), retrievedTask.getDescription(),
                "Описание задачи не должно изменяться.");
        assertEquals(task.getStatus(), retrievedTask.getStatus(),
                "Статус задачи не должен изменяться.");
    }

    @Test
    void testSubtaskOnAddition() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);

        Subtask retrievedSubtask = taskManager.getSubtask(subtask.getId());
        assertEquals(subtask.getTitle(), retrievedSubtask.getTitle(),
                "Название подзадачи не должно изменяться.");
        assertEquals(subtask.getDescription(), retrievedSubtask.getDescription(),
                "Описание подзадачи не должно изменяться.");
        assertEquals(subtask.getEpicId(), retrievedSubtask.getEpicId(),
                "ID родительского эпика подзадачи не должен изменяться.");
        assertEquals(subtask.getStatus(), retrievedSubtask.getStatus(),
                "Статус подзадачи не должен изменяться.");
    }

    @Test
    void testEpicOnAddition() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Epic retrievedEpic = taskManager.getEpic(epic.getId());
        assertEquals(epic.getTitle(), retrievedEpic.getTitle(),
                "Название эпика не должно изменяться.");
        assertEquals(epic.getDescription(), retrievedEpic.getDescription(),
                "Описание эпика не должно изменяться.");
        assertEquals(epic.getStatus(), retrievedEpic.getStatus(),
                "Статус эпика не должен изменяться.");
    }

    @Test
    void testSubtaskIdRemovedFromEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);
        int subtaskId = createdSubtask.getId();

        taskManager.deleteSubtask(subtaskId);

        Epic retrievedEpic = taskManager.getEpic(epic.getId());
        assertFalse(retrievedEpic.getSubtaskIds().contains(subtaskId),
                "ID подзадачи не должен сохраняться в эпике после её удаления.");
    }

    @Test
    void testSubtaskRemovedFromHistory() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);
        taskManager.getSubtask(createdSubtask.getId());

        taskManager.deleteSubtask(createdSubtask.getId());

        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(createdSubtask),
                "Подзадача не должна сохраняться в истории после её удаления.");
    }

    @Test
    void testOverlappingSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask(
                "Подзадача 1",
                "Описание подзадачи 1",
                Duration.ofHours(3),
                LocalDateTime.now().plusDays(1),
                epic.getId()
        );
        Subtask subtask2 = new Subtask(
                "Подзадача 2",
                "Описание подзадачи 2",
                Duration.ofHours(2),
                LocalDateTime.now().plusDays(1).plusHours(1),
                epic.getId()
        );

        taskManager.createSubtask(subtask1);

        assertThrows(OverlapException.class, () -> taskManager.createSubtask(subtask2),
                "Задача пересекается по времени с другой задачей");
    }
}

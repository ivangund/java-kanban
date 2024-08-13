package test;

import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTests {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testEpicEquals() {
        Epic epic1 = new Epic(1, "Эпик", "Описание", Status.NEW);
        Epic epic2 = new Epic(1, "Эпик", "Описание", Status.NEW);

        assertEquals(epic1, epic2, "Эпики должны быть равны, если их id совпадают.");
    }

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW);

        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(epic.getId()),
                "Эпик не может содержать себя в качестве подзадачи");
    }

    @Test
    void testEpicStatusAllNewSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.NEW);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.NEW, epic.getStatus(),
                "Статус эпика, содержащего только подзадачи со статусом NEW, должен быть NEW");
    }

    @Test
    void testEpicStatusAllDoneSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(),
                "Статус эпика, содержащего только подзадачи со статусом DONE, должен быть DONE");
    }

    @Test
    void testEpicStatusNewAndDoneSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика, содержащего только подзадачи со статусами NEW и DONE, должен быть IN_PROGRESS");
    }

    @Test
    void testEpicStatusAllInProgressSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", epic.getId());
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(),
                "Статус эпика, содержащего только подзадачи со статусом IN_PROGRESS, должен быть IN_PROGRESS");
    }
}

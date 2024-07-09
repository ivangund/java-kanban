package test.java.ru.practicum.tasktracker;

import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.tasks.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTests {

    @Test
    void testTaskEquals() {
        Task task1 = new Task(1, "Задача", "Описание", Status.NEW);
        Task task2 = new Task(1, "Задача", "Описание", Status.NEW);

        assertEquals(task1, task2, "Задачи должны быть равны, если их id совпадают.");
    }
}

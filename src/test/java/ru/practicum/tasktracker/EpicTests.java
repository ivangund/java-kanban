package test.java.ru.practicum.tasktracker;

import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTests {

    @Test
    void testEpicEquals() {
        Epic epic1 = new Epic(1, "Эпик", "Описание", Status.NEW);
        Epic epic2 = new Epic(1, "Эпик", "Описание", Status.NEW);

        assertEquals(epic1, epic2, "Эпики должны быть равны, если их id совпадают.");
    }

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic(1, "Эпик", "Описание", Status.NEW);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> epic.addSubtaskId(epic.getId()));
        assertEquals("Эпик не может содержать себя в качестве подзадачи", thrown.getMessage());
    }
}

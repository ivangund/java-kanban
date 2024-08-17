package test;

import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTests {

    @Test
    void testSubtaskEquals() {
        Subtask subtask1 = new Subtask(1, "Подзадача", "Описание", Status.NEW, 2);
        Subtask subtask2 = new Subtask(1, "Подзадача", "Описание", Status.NEW, 2);

        assertEquals(subtask1, subtask2, "Подзадачи должны быть равны, если их id совпадают.");
    }

    @Test
    void testSubtaskCannotHaveItselfAsEpic() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Subtask subtask = new Subtask(1, "Подзадача", "Описание", Status.NEW, 1);
        });
        assertEquals("Подзадача не может быть своим же эпиком", thrown.getMessage());
    }
}

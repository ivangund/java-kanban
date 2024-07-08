package test.java.ru.practicum.tasktracker;

import main.java.ru.practicum.tasktracker.managers.HistoryManager;
import main.java.ru.practicum.tasktracker.managers.Managers;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTests {

    @Test
    void testTaskManagerInitialization() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Должен возвращаться объект TaskManager.");
    }

    @Test
    void testHistoryManagerInitialization() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Должен возвращаться объект HistoryManager.");
    }

}

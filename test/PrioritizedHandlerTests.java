package test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.server.HttpTaskServer;
import main.java.ru.practicum.tasktracker.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHandlerTests {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final HttpClient client;

    public PrioritizedHandlerTests() {
        this.client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        createTestTask("Задача 1", "Описание задачи 1", Duration.ofHours(1), LocalDateTime.now());
        createTestTask("Задача 2", "Описание задачи 2", Duration.ofHours(2),
                LocalDateTime.now().plusDays(1));

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/prioritized");

        assertEquals(200, response.statusCode(), "Не удалось получить приоритетные задачи");

        JsonArray prioritizedArray = JsonParser.parseString(response.body()).getAsJsonArray();

        List<Task> expectedPrioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks.size(), prioritizedArray.size(),
                "Некорректное количество задач в приоритетном списке");

        for (int i = 0; i < expectedPrioritizedTasks.size(); i++) {
            JsonObject taskJson = prioritizedArray.get(i).getAsJsonObject();
            Task expectedTask = expectedPrioritizedTasks.get(i);

            assertEquals(expectedTask.getTitle(), taskJson.get("title").getAsString(),
                    "Некорректное имя задачи в приоритетном списке");
            assertEquals(expectedTask.getId(), taskJson.get("id").getAsInt(),
                    "Некорректный ID задачи в приоритетном списке");
        }
    }

    private HttpResponse<String> sendGetRequest(String uri)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Task createTestTask(String name, String description, Duration duration,
            LocalDateTime startTime) {
        Task task = new Task(name, description, duration, startTime);
        manager.createTask(task);
        return task;
    }
}

package test;

import com.google.gson.*;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.server.HttpTaskServer;
import main.java.ru.practicum.tasktracker.server.adapters.DurationAdapter;
import main.java.ru.practicum.tasktracker.server.adapters.LocalDateTimeAdapter;
import main.java.ru.practicum.tasktracker.server.adapters.TaskAdapter;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
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

public class SubtaskHandlerTests {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson;
    private final HttpClient client;

    public SubtaskHandlerTests() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .create();
        this.client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        manager.createEpic(new Epic("Эпик", "Описание эпика"));
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", Duration.ofMinutes(5),
                LocalDateTime.now(), 1);

        String subtaskJson = gson.toJson(subtask);

        JsonObject taskJsonObject = JsonParser.parseString(subtaskJson).getAsJsonObject();

        taskJsonObject.remove("id");
        taskJsonObject.remove("status");

        HttpResponse<String> response = sendPostRequest("http://localhost:8080/subtasks",
                taskJsonObject.toString());

        assertEquals(201, response.statusCode(), "Подзадача не была добавлена");

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask.getTitle(), subtasksFromManager.getFirst().getTitle(),
                "Некорректное имя подзадачи");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Task subtask = createTestSubtask("Подзадача 1", "Описание подзадачи 1", Duration.ofHours(1),
                LocalDateTime.now(), 1);
        int subtaskId = subtask.getId();

        HttpResponse<String> response = sendGetRequest(
                "http://localhost:8080/subtasks/" + subtaskId);

        assertEquals(200, response.statusCode(), "Не удалось получить подзадачу");

        Task returnedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getTitle(), returnedSubtask.getTitle(), "Имена подзадач не совпадают");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Task subtask1 = createTestSubtask("Подзадача 1", "Описание подзадачи 1",
                Duration.ofHours(1), LocalDateTime.now(), 1);
        Task subtask2 = createTestSubtask("Подзадача 2", "Описание подзадачи 2",
                Duration.ofHours(1), LocalDateTime.now().plusDays(1), 1);

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/subtasks");

        assertEquals(200, response.statusCode(), "Не удалось получить список подзадач");

        JsonArray subtasksArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(2, subtasksArray.size(), "Некорректное количество подзадач в ответе");

        JsonObject subtask1Json = subtasksArray.get(0).getAsJsonObject();
        JsonObject subtask2Json = subtasksArray.get(1).getAsJsonObject();

        assertEquals(subtask1.getTitle(), subtask1Json.get("title").getAsString(),
                "Некорректное имя первой задачи");
        assertEquals(subtask2.getTitle(), subtask2Json.get("title").getAsString(),
                "Некорректное имя второй задачи");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Task subtask = createTestSubtask("Подзадача 1", "Описание подзадачи 1", Duration.ofHours(1),
                LocalDateTime.now(), 1);
        subtask.setTitle("Обновленное имя подзадачи 1");
        String subtaskJson = gson.toJson(subtask);

        HttpResponse<String> response = sendPostRequest("http://localhost:8080/subtasks",
                subtaskJson);

        assertEquals(200, response.statusCode(), "Подзадача не была обновлена");

        Task updatedSubtask = manager.getSubtask(subtask.getId());
        assertEquals(subtask.getTitle(), updatedSubtask.getTitle(), "Имя задачи не было обновлено");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Task subtask = createTestSubtask("Подзадача 1", "Описание подзадачи 1", Duration.ofHours(1),
                LocalDateTime.now(), 1);
        int subtaskId = subtask.getId();

        HttpResponse<String> response = sendDeleteRequest(
                "http://localhost:8080/subtasks/" + subtaskId);

        assertEquals(200, response.statusCode(), "Подзадача не была удалена");

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertTrue(subtasksFromManager.isEmpty(), "Подзадача не была удалена из менеджера");
    }

    private HttpResponse<String> sendPostRequest(String uri, String body)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGetRequest(String uri)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDeleteRequest(String uri)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Task createTestSubtask(String name, String description, Duration duration,
            LocalDateTime startTime, int epicId) {
        Subtask subtask = new Subtask(name, description, duration, startTime, epicId);
        manager.createSubtask(subtask);
        return subtask;
    }
}

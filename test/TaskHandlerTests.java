package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.server.HttpTaskServer;
import main.java.ru.practicum.tasktracker.server.adapters.LocalDateTimeAdapter;
import main.java.ru.practicum.tasktracker.server.adapters.DurationAdapter;
import main.java.ru.practicum.tasktracker.server.adapters.TaskAdapter;
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

public class TaskHandlerTests {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson;
    private final HttpClient client;

    public TaskHandlerTests() {
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
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Задача 1", "Описание задачи 1", Duration.ofMinutes(5),
                LocalDateTime.now());

        String taskJson = gson.toJson(task);

        JsonObject taskJsonObject = JsonParser.parseString(taskJson).getAsJsonObject();

        taskJsonObject.remove("id");
        taskJsonObject.remove("status");

        HttpResponse<String> response = sendPostRequest("http://localhost:8080/tasks",
                taskJsonObject.toString());

        assertEquals(201, response.statusCode(), "Задача не была добавлена");

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getTitle(), tasksFromManager.getFirst().getTitle(),
                "Некорректное имя задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = createTestTask("Задача 1", "Описание задачи 1", Duration.ofHours(1),
                LocalDateTime.now());
        int taskId = task.getId();

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/tasks/" + taskId);

        assertEquals(200, response.statusCode(), "Не удалось получить задачу");

        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getTitle(), returnedTask.getTitle(), "Имена задач не совпадают");
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task1 = createTestTask("Задача 1", "Описание задачи 1", Duration.ofHours(1),
                LocalDateTime.now());
        Task task2 = createTestTask("Задача 2", "Описание задачи 2", Duration.ofHours(1),
                LocalDateTime.now().plusDays(1));

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/tasks");

        assertEquals(200, response.statusCode(), "Не удалось получить список задач");

        JsonArray tasksArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(2, tasksArray.size(), "Некорректное количество задач в ответе");

        JsonObject task1Json = tasksArray.get(0).getAsJsonObject();
        JsonObject task2Json = tasksArray.get(1).getAsJsonObject();

        assertEquals(task1.getTitle(), task1Json.get("title").getAsString(),
                "Некорректное имя первой задачи");
        assertEquals(task2.getTitle(), task2Json.get("title").getAsString(),
                "Некорректное имя второй задачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = createTestTask("Задача 1", "Описание задачи 1", Duration.ofHours(1),
                LocalDateTime.now());
        task.setTitle("Обновленное имя задачи 1");
        String taskJson = gson.toJson(task);

        HttpResponse<String> response = sendPostRequest("http://localhost:8080/tasks", taskJson);

        assertEquals(200, response.statusCode(), "Задача не была обновлена");

        Task updatedTask = manager.getTask(task.getId());
        assertEquals(task.getTitle(), updatedTask.getTitle(), "Имя задачи не было обновлено");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = createTestTask("Задача 1", "Описание задачи 1", Duration.ofHours(1),
                LocalDateTime.now());
        int taskId = task.getId();

        HttpResponse<String> response = sendDeleteRequest("http://localhost:8080/tasks/" + taskId);

        assertEquals(200, response.statusCode(), "Задача не была удалена");

        List<Task> tasksFromManager = manager.getTasks();
        assertTrue(tasksFromManager.isEmpty(), "Задача не была удалена из менеджера");
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

    private Task createTestTask(String name, String description, Duration duration,
            LocalDateTime startTime) {
        Task task = new Task(name, description, duration, startTime);
        manager.createTask(task);
        return task;
    }
}

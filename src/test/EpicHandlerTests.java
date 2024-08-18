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
import main.java.ru.practicum.tasktracker.server.adapters.EpicAdapter;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
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

public class EpicHandlerTests {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private final Gson gson;
    private final HttpClient client;

    public EpicHandlerTests() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");

        String epicJson = gson.toJson(epic);

        JsonObject epicJsonObject = JsonParser.parseString(epicJson).getAsJsonObject();

        epicJsonObject.remove("id");
        epicJsonObject.remove("status");

        HttpResponse<String> response = sendPostRequest("http://localhost:8080/epics",
                epicJsonObject.toString());

        assertEquals(201, response.statusCode(), "Эпик не был добавлен");

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic.getTitle(), epicsFromManager.getFirst().getTitle(),
                "Некорректное имя эпика");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Эпик 1", "Описание эпика 1");
        int epicId = epic.getId();

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/epics/" + epicId);

        assertEquals(200, response.statusCode(), "Не удалось получить эпик");

        Epic returnedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getTitle(), returnedEpic.getTitle(), "Имена эпиков не совпадают");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic1 = createTestEpic("Эпик 1", "Описание эпика 1");
        Epic epic2 = createTestEpic("Эпик 2", "Описание эпика 2");

        HttpResponse<String> response = sendGetRequest("http://localhost:8080/epics");

        assertEquals(200, response.statusCode(), "Не удалось получить список эпиков");

        JsonArray epicsArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(2, epicsArray.size(), "Некорректное количество эпиков в ответе");

        JsonObject epic1Json = epicsArray.get(0).getAsJsonObject();
        JsonObject epic2Json = epicsArray.get(1).getAsJsonObject();

        assertEquals(epic1.getTitle(), epic1Json.get("title").getAsString(),
                "Некорректное имя первого эпика");
        assertEquals(epic2.getTitle(), epic2Json.get("title").getAsString(),
                "Некорректное имя второго эпика");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Эпик 1", "Описание эпика 1");
        int epicId = epic.getId();

        HttpResponse<String> response = sendDeleteRequest("http://localhost:8080/epics/" + epicId);

        assertEquals(200, response.statusCode(), "Эпик не был удален");

        List<Epic> epicsFromManager = manager.getEpics();
        assertTrue(epicsFromManager.isEmpty(), "Эпик не был удален из менеджера");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = createTestEpic("Эпик с подзадачами", "Описание эпика с подзадачами");
        int epicId = epic.getId();

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Duration.ofHours(1),
                LocalDateTime.now(), epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Duration.ofHours(2),
                LocalDateTime.now().plusHours(1), epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        HttpResponse<String> response = sendGetRequest(
                "http://localhost:8080/epics/" + epicId + "/subtasks");

        assertEquals(200, response.statusCode(), "Не удалось получить подзадачи эпика");

        JsonArray subtasksArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(2, subtasksArray.size(), "Некорректное количество подзадач в ответе");

        JsonObject subtask1Json = subtasksArray.get(0).getAsJsonObject();
        JsonObject subtask2Json = subtasksArray.get(1).getAsJsonObject();

        assertEquals(subtask1.getTitle(), subtask1Json.get("title").getAsString(),
                "Некорректное имя первой подзадачи");
        assertEquals(subtask2.getTitle(), subtask2Json.get("title").getAsString(),
                "Некорректное имя второй подзадачи");
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

    private Epic createTestEpic(String name, String description) {
        Epic epic = new Epic(name, description);
        manager.createEpic(epic);
        return epic;
    }
}

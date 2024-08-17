package main.java.ru.practicum.tasktracker.server.handlers;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EpicHandler extends TaskHandler {

    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        super();
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] splitPath = path.split("/");
        if (splitPath.length == 2) {
            switch (method) {
                case "GET" -> handleGetTasks(exchange);
                case "POST" -> handlePostTask(exchange);
                case null, default -> sendResponse(exchange, "Метод не разрешен.", 405);
            }
        } else if (splitPath.length == 3) {
            int id = Integer.parseInt(splitPath[2]);
            switch (method) {
                case "GET" -> handleGetTaskById(exchange, id);
                case "DELETE" -> handleDeleteTask(exchange, id);
                case null, default -> sendResponse(exchange, "Метод не разрешен.", 405);
            }
        } else if (splitPath.length == 4 && path.endsWith("/subtasks") && method.equals("GET")) {
            int id = Integer.parseInt(splitPath[2]);
            handleGetEpicSubtasks(exchange, id);
        }
    }

    @Override
    protected void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getEpics();
        String response = gson.toJson(epics);
        sendResponse(exchange, response, 200);
    }

    @Override
    protected void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Epic epic = taskManager.getEpic(id);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            String response = gson.toJson(epic);
            sendResponse(exchange, response, 200);
        }
    }

    @Override
    protected void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(),
                    StandardCharsets.UTF_8);
            Epic epic = gson.fromJson(reader, Epic.class);
            handleCreateEpic(exchange, epic);
        } catch (JsonParseException | DateTimeParseException e) {
            sendResponse(exchange, String.format("Не удалось добавить эпик (%s).", e.getMessage()),
                    400);
        }
    }

    @Override
    protected void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        boolean result = taskManager.deleteEpic(id);
        if (result) {
            sendResponse(exchange, "Эпик с ID " + id + " успешно удален.", 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateEpic(HttpExchange exchange, Epic epic) throws IOException {
        taskManager.createEpic(new Epic(epic.getTitle(), epic.getDescription()));
        sendResponse(exchange, "Эпик успешно добавлен.", 201);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getEpic(id) != null) {
            List<Subtask> subtasks = taskManager.getEpicSubtasks(id);
            String response = gson.toJson(subtasks);
            sendResponse(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }
}

package main.java.ru.practicum.tasktracker.server.handlers;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;
import main.java.ru.practicum.tasktracker.exceptions.OverlapException;
import main.java.ru.practicum.tasktracker.exceptions.UnknownStatusException;
import main.java.ru.practicum.tasktracker.managers.InMemoryTaskManager;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Task;

public class TaskHandler extends BaseHttpHandler {

    private final TaskManager taskManager;

    public TaskHandler() {
        super();
        taskManager = new InMemoryTaskManager();
    }

    public TaskHandler(TaskManager taskManager) {
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
        }
    }

    protected void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        String response = gson.toJson(tasks);
        sendResponse(exchange, response, 200);
    }

    protected void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Task task = taskManager.getTask(id);
        if (task == null) {
            sendNotFound(exchange);
        } else {
            String response = gson.toJson(task);
            sendResponse(exchange, response, 200);
        }
    }

    protected void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(),
                    StandardCharsets.UTF_8);
            Task task = gson.fromJson(reader, Task.class);

            if (task.getId() == 0) {
                // Запрос для добавления новой задачи
                handleCreateTask(exchange, task);
            } else {
                // Запрос для обновления задачи
                handleUpdateTask(exchange, task);
            }
        } catch (JsonParseException | DateTimeParseException | OverlapException |
                 UnknownStatusException e) {
            sendResponse(exchange,
                    String.format("Не удалось добавить задачу (%s).", e.getMessage()), 400);
        }
    }

    protected void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        boolean result = taskManager.deleteTask(id);
        if (result) {
            sendResponse(exchange, "Задача с ID " + id + " успешно удалена.", 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateTask(HttpExchange exchange, Task task) throws IOException {
        taskManager.createTask(new Task(task.getTitle(), task.getDescription(), task.getDuration(),
                task.getStartTime()));
        sendResponse(exchange, "Задача успешно добавлена.", 201);
    }

    private void handleUpdateTask(HttpExchange exchange, Task task) throws IOException {
        if (task.getStatus() != null) {
            Task existingTask = taskManager.getTask(task.getId());
            if (existingTask != null) {
                taskManager.updateTask(task);
                sendResponse(exchange, "Задача успешно обновлена.", 200);
            } else {
                sendResponse(exchange, "Не удалось обновить задачу (Задача с ID "
                        + task.getId() + " не существует).", 404);
            }
        } else {
            sendResponse(exchange, "Не удалось обновить задачу (Отсутствует поле status).",
                    400);
        }
    }
}

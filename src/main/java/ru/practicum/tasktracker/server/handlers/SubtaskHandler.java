package main.java.ru.practicum.tasktracker.server.handlers;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import main.java.ru.practicum.tasktracker.exceptions.OverlapException;
import main.java.ru.practicum.tasktracker.exceptions.UnknownStatusException;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SubtaskHandler extends TaskHandler {

    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        super();
        this.taskManager = taskManager;
    }

    @Override
    protected void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getSubtasks();
        String response = gson.toJson(subtasks);
        sendResponse(exchange, response, 200);
    }

    @Override
    protected void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Subtask subtask = taskManager.getSubtask(id);
        if (subtask == null) {
            sendNotFound(exchange);
        } else {
            String response = gson.toJson(subtask);
            sendResponse(exchange, response, 200);
        }
    }

    @Override
    protected void handlePostTask(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(),
                    StandardCharsets.UTF_8);
            Subtask subtask = gson.fromJson(reader, Subtask.class);

            if (subtask.getId() == 0) {
                // Запрос для добавления новой подзадачи
                handleCreateSubtask(exchange, subtask);
            } else {
                // Запрос для обновления подзадачи
                handleUpdateSubtask(exchange, subtask);
            }
        } catch (JsonParseException | DateTimeParseException | OverlapException |
                 UnknownStatusException e) {
            sendResponse(exchange,
                    String.format("Не удалось добавить подзадачу (%s).", e.getMessage()), 400);
        }
    }

    @Override
    protected void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        boolean result = taskManager.deleteSubtask(id);
        if (result) {
            sendResponse(exchange, "Подзадача с ID " + id + " успешно удалена.", 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateSubtask(HttpExchange exchange, Subtask task) throws IOException {
        taskManager.createSubtask(
                new Subtask(task.getTitle(), task.getDescription(), task.getDuration(),
                        task.getStartTime(), task.getEpicId()));
        sendResponse(exchange, "Подзадача успешно добавлена.", 201);
    }

    private void handleUpdateSubtask(HttpExchange exchange, Subtask subtask) throws IOException {
        if (subtask.getStatus() != null) {
            Subtask existingSubtask = taskManager.getSubtask(subtask.getId());
            if (existingSubtask != null) {
                taskManager.updateSubtask(subtask);
                sendResponse(exchange, "Подзадача успешно обновлена.", 200);
            } else {
                sendResponse(exchange, "Не удалось обновить подзадачу (Подзадача с ID "
                        + subtask.getId() + " не существует).", 404);
            }
        } else {
            sendResponse(exchange, "Не удалось обновить подзадачу (Отсутствует поле status).",
                    400);
        }
    }
}

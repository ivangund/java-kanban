package main.java.ru.practicum.tasktracker.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Task;
import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        super();
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            handleGetHistory(exchange);
        } else {
            sendResponse(exchange, "Метод не разрешен.", 405);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String response = gson.toJson(history);
        sendResponse(exchange, response, 200);
    }
}

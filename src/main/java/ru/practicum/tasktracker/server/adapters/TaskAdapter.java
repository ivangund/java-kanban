package main.java.ru.practicum.tasktracker.server.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import main.java.ru.practicum.tasktracker.exceptions.UnknownStatusException;
import main.java.ru.practicum.tasktracker.tasks.Task;
import main.java.ru.practicum.tasktracker.enums.Status;

public class TaskAdapter extends TypeAdapter<Task> {

    private final TypeAdapter<LocalDateTime> localDateTimeAdapter;
    private final TypeAdapter<Duration> durationAdapter;

    public TaskAdapter() {
        localDateTimeAdapter = new LocalDateTimeAdapter();
        durationAdapter = new DurationAdapter();
    }

    @Override
    public void write(JsonWriter out, Task task) throws IOException {
        out.beginObject();
        out.name("id").value(task.getId());
        out.name("title").value(task.getTitle());
        out.name("description").value(task.getDescription());
        out.name("startTime");
        localDateTimeAdapter.write(out, task.getStartTime());
        out.name("duration");
        durationAdapter.write(out, task.getDuration());
        out.name("status").value(task.getStatus() != null ? task.getStatus().name() : null);
        out.endObject();
    }

    @Override
    public Task read(JsonReader in) {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

        String title = getAsString(jsonObject, "title");
        String description = getAsString(jsonObject, "description");
        LocalDateTime startTime = localDateTimeAdapter.fromJsonTree(
                getAsJsonElement(jsonObject, "startTime"));
        Duration duration = durationAdapter.fromJsonTree(getAsJsonElement(jsonObject, "duration"));

        int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : 0;

        Status status = null;
        if (jsonObject.has("status")) {
            try {
                status = Status.valueOf(jsonObject.get("status").getAsString());
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException("Неизвестный статус задачи");
            }
        }

        return new Task(id, title, description, status, duration, startTime);
    }

    private String getAsString(JsonObject jsonObject, String memberName) throws JsonParseException {
        JsonElement element = jsonObject.get(memberName);
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("Отсутствует поле " + memberName);
        }
        return element.getAsString();
    }

    private JsonElement getAsJsonElement(JsonObject jsonObject, String memberName)
            throws JsonParseException {
        JsonElement element = jsonObject.get(memberName);
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("Отсутствует поле " + memberName);
        }
        return element;
    }
}

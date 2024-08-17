package main.java.ru.practicum.tasktracker.server.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.exceptions.UnknownStatusException;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskAdapter extends TypeAdapter<Subtask> {

    private final TypeAdapter<LocalDateTime> localDateTimeAdapter;
    private final TypeAdapter<Duration> durationAdapter;

    public SubtaskAdapter() {
        localDateTimeAdapter = new LocalDateTimeAdapter();
        durationAdapter = new DurationAdapter();
    }

    @Override
    public void write(JsonWriter out, Subtask subtask) throws IOException {
        out.beginObject();
        out.name("id").value(subtask.getId());
        out.name("title").value(subtask.getTitle());
        out.name("description").value(subtask.getDescription());
        out.name("startTime");
        localDateTimeAdapter.write(out, subtask.getStartTime());
        out.name("duration");
        durationAdapter.write(out, subtask.getDuration());
        out.name("status").value(subtask.getStatus() != null ? subtask.getStatus().name() : null);
        out.name("epicId").value(subtask.getEpicId());
        out.endObject();
    }

    @Override
    public Subtask read(JsonReader in) {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

        String title = getAsString(jsonObject, "title");
        String description = getAsString(jsonObject, "description");
        LocalDateTime startTime = localDateTimeAdapter.fromJsonTree(
                getAsJsonElement(jsonObject, "startTime"));
        Duration duration = durationAdapter.fromJsonTree(getAsJsonElement(jsonObject, "duration"));
        String epicId = getAsString(jsonObject, "epicId");

        int id = jsonObject.has("id") ? jsonObject.get("id").getAsInt() : 0;

        Status status = null;
        if (jsonObject.has("status")) {
            try {
                status = Status.valueOf(jsonObject.get("status").getAsString());
            } catch (IllegalArgumentException e) {
                throw new UnknownStatusException("Неизвестный статус подзадачи");
            }
        }

        return new Subtask(id, title, description, status, duration, startTime,
                Integer.parseInt(epicId));
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

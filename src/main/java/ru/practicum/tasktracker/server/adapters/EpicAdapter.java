package main.java.ru.practicum.tasktracker.server.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class EpicAdapter extends TypeAdapter<Epic> {

    private final TypeAdapter<LocalDateTime> localDateTimeAdapter;
    private final TypeAdapter<Duration> durationAdapter;

    public EpicAdapter() {
        localDateTimeAdapter = new LocalDateTimeAdapter();
        durationAdapter = new DurationAdapter();
    }

    @Override
    public void write(JsonWriter out, Epic epic) throws IOException {
        out.beginObject();
        out.name("id").value(epic.getId());
        out.name("title").value(epic.getTitle());
        out.name("description").value(epic.getDescription());
        if (epic.getStartTime() != null) {
            out.name("startTime");
            localDateTimeAdapter.write(out, epic.getStartTime());
        }
        out.name("duration");
        durationAdapter.write(out, epic.getDuration());
        out.name("status").value(epic.getStatus() != null ? epic.getStatus().name() : null);
        out.name("subtaskIds").value(epic.getSubtaskIds().toString());
        out.endObject();
    }

    @Override
    public Epic read(JsonReader in) {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();

        String title = getAsString(jsonObject, "title");
        String description = getAsString(jsonObject, "description");

        return new Epic(title, description);
    }

    private String getAsString(JsonObject jsonObject, String memberName) throws JsonParseException {
        JsonElement element = jsonObject.get(memberName);
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("Отсутствует поле " + memberName);
        }
        return element.getAsString();
    }
}

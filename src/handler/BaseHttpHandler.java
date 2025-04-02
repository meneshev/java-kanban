package handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class BaseHttpHandler {

    public final static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.serializeNulls();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gson = gsonBuilder.create();
    }

    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public Gson getGson() {
        return gson;
    }

    protected Integer getIdFromPath(HttpExchange exchange) {
        return Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
    }

    protected Boolean hasDatesIntersect(Task task) {
        if (taskManager.getClass() == InMemoryTaskManager.class) {
            return ((InMemoryTaskManager) taskManager).hasDateIntersect(task);
        } else {
            return null;
        }
    }

    protected abstract EndPoint getEndPoint(String requestPath, HttpMethod method);

    protected void handleIncorrectRequest(HttpExchange exchange) throws IOException {
        byte[] resp = "Такого эндпоинта не существует".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendText(HttpExchange exchange, String text, int status) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(status, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        // TODO указать неизвестный идентификатор
        byte[] resp = "Объект не найден".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        byte[] resp = "Задача не добавлена так как пересекается с текущими по срокам выполнения"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange) throws IOException {
        byte[] resp = "Произошла неожиданная ошибка".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(500, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }
}

class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(dtf));
        } else {
            jsonWriter.value((String) null);
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        return switch (token) {
            case JsonToken.NULL -> {
                jsonReader.nextNull();
                yield null;
            }
            case JsonToken.STRING -> LocalDateTime.parse(jsonReader.nextString(), dtf);
            default -> throw new RuntimeException("Unexpected token type " + token.name());
        };
    }
}

class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration != null) {
            jsonWriter.value(duration.toMinutes());
        } else {
            jsonWriter.value((String) null);
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        return switch (token) {
            case JsonToken.NULL -> {
                jsonReader.nextNull();
                yield null;
            }
            case JsonToken.NUMBER -> Duration.ofMinutes(jsonReader.nextInt());
            default -> throw new RuntimeException("Unexpected token type " + token.name());
        };
    }
}

class TaskListTypeToken extends TypeToken<List<Task>> {

}

class SubtaskListTypeToken extends TypeToken<List<Subtask>> {

}

class EpicListTypeToken extends TypeToken<List<Epic>> {

}
package handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import controller.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import util.ObjectBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    public HistoryHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.clearTasks();
        taskServer.startServer();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stopServer();
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        manager.addTask(task);

        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        manager.addTask(epic);

        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        manager.addTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getHistoryRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/history"))
                .build();
        HttpResponse<String> response = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getHistory().isEmpty());

        HttpRequest getTaskRequest= HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .build();
        response = client.send(getTaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getEpicRequest= HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/epics/2"))
                .build();
        response = client.send(getEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        HttpRequest getSubtaskRequest= HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks/3"))
                .build();
        response = client.send(getSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        response = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        assertEquals(3, jsonArray.size());
    }
}

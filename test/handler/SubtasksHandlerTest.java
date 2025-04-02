package handler;

import com.google.gson.*;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = BaseHttpHandler.gson;

    public SubtasksHandlerTest() throws IOException {
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
    public void testGetSubtasks() throws IOException, InterruptedException {
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

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Subtask> subtasksFromHttpApi = gson.fromJson(jsonArray, new SubtaskListTypeToken().getType());
        assertArrayEquals(manager.getTasksByType(Subtask.class).toArray(), subtasksFromHttpApi.toArray());
    }

    @Test
    public void testGetSubtasksById() throws IOException, InterruptedException {
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

        Integer subtaskId = subtask.getId();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        Task taskFromHttpApi = gson.fromJson(response.body(), Subtask.class);
        assertEquals(manager.getTaskById(subtaskId), taskFromHttpApi);

        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks/" + 500))
                .header("Accept", "application/json")
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testPostSubtasks() throws IOException, InterruptedException {
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
                .with(Task::setStartTime, LocalDateTime.of(2025, Month.APRIL, 1, 15, 0))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Subtask.class).isEmpty());

        String subtaskJson = gson.toJson(subtask);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .uri(URI.create("http://localhost:8080/subtasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task subtaskWithDateIntersection = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .with(Task::setStartTime, LocalDateTime.of(2025, Month.APRIL, 1, 15, 15))
                .build();
        subtaskJson = gson.toJson(subtaskWithDateIntersection);
        postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .uri(URI.create("http://localhost:8080/subtasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());

        Subtask changedSubtask = (Subtask) manager.getTaskById(2);
        changedSubtask.setName("New name");
        changedSubtask.setDescription("New description");
        subtaskJson = gson.toJson(changedSubtask);
        postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .uri(URI.create("http://localhost:8080/subtasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        Subtask changedSubtaskFromHttpApi = gson.fromJson(jsonObject, Subtask.class);
        assertEquals(changedSubtaskFromHttpApi, changedSubtask);

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        List<Subtask> subtasksFromHttpApi = gson.fromJson(jsonArray, new SubtaskListTypeToken().getType());
        assertArrayEquals(manager.getTasksByType(Subtask.class).toArray(), subtasksFromHttpApi.toArray());
    }

    @Test
    public void testDeleteSubtasksById() throws IOException, InterruptedException {
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
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Subtask> subtasksFromHttpApi = gson.fromJson(jsonArray, new SubtaskListTypeToken().getType());
        assertEquals(1, subtasksFromHttpApi.size());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .build();

        response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Subtask.class).isEmpty());
    }
}


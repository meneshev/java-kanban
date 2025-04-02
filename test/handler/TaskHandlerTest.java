package handler;

import adapter.TaskListTypeToken;
import com.google.gson.*;
import controller.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class TaskHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = BaseHttpHandler.gson;

    public TaskHandlerTest() throws IOException {
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
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Task> tasksFromHttpApi = gson.fromJson(jsonArray, new TaskListTypeToken().getType());
        assertArrayEquals(manager.getAllTasks().toArray(), tasksFromHttpApi.toArray());
    }

    @Test
    public void testGetTasksById() throws IOException, InterruptedException {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        manager.addTask(task);
        Integer taskId = task.getId();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        Task taskFromHttpApi = gson.fromJson(response.body(), Task.class);
        assertEquals(manager.getTaskById(taskId), taskFromHttpApi);

        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks/" + 2))
                .header("Accept", "application/json")
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testPostTasks() throws IOException, InterruptedException {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .with(Task::setStartTime, LocalDateTime.of(2025, Month.APRIL, 1, 15, 0))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest GetRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks"))
                .build();
        HttpResponse<String> response = client.send(GetRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Task.class).isEmpty());

        String taskJson = gson.toJson(task);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(URI.create("http://localhost:8080/tasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task taskWithDateIntersection = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .with(Task::setStartTime, LocalDateTime.of(2025, Month.APRIL, 1, 15, 15))
                .build();
        taskJson = gson.toJson(taskWithDateIntersection);
        postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(URI.create("http://localhost:8080/tasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());

        Task changedTask = manager.getTaskById(1);
        changedTask.setName("New name");
        changedTask.setDescription("New description");
        taskJson = gson.toJson(changedTask);
        postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(URI.create("http://localhost:8080/tasks"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        response = client.send(GetRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        Task changedTaskFromHttpApi = gson.fromJson(jsonObject, Task.class);
        assertEquals(changedTaskFromHttpApi, changedTask);


        response = client.send(GetRequest, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        List<Task> tasksFromHttpApi = gson.fromJson(jsonArray, new TaskListTypeToken().getType());
        assertArrayEquals(manager.getTasksByType(Task.class).toArray(), tasksFromHttpApi.toArray());
    }

    @Test
    public void testDeleteTasksById() throws IOException, InterruptedException {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Task> tasksFromHttpApi = gson.fromJson(jsonArray, new TaskListTypeToken().getType());
        assertEquals(1, tasksFromHttpApi.size());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .build();

        response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Task.class).isEmpty());
    }
}

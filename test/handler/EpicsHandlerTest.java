package handler;

import com.google.gson.*;
import controller.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import util.ObjectBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicsHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = BaseHttpHandler.gson;

    public EpicsHandlerTest() throws IOException {
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
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        manager.addTask(epic);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Epic> epicsFromHttpApi = gson.fromJson(jsonArray, new EpicListTypeToken().getType());
        assertArrayEquals(manager.getTasksByType(Epic.class).toArray(), epicsFromHttpApi.toArray());
    }

    @Test
    public void testGetEpicsById() throws IOException, InterruptedException {
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        manager.addTask(epic);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject());
        Epic epicFromHttpApi = gson.fromJson(response.body(), Epic.class);
        assertEquals(manager.getTaskById(epic.getId()), epicFromHttpApi);

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

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/epics"))
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Epic.class).isEmpty());

        String epicJson = gson.toJson(epic);
        HttpRequest postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .uri(URI.create("http://localhost:8080/epics"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());


        Epic changedEpic = (Epic) manager.getTaskById(1);
        changedEpic.setName("New name");
        changedEpic.setDescription("New description");
        epicJson = gson.toJson(changedEpic);
        postRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .uri(URI.create("http://localhost:8080/epics"))
                .build();
        response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        JsonObject jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
        Epic changedEpicFromHttpApi = gson.fromJson(jsonObject, Epic.class);
        assertEquals(changedEpicFromHttpApi, changedEpic);

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        List<Epic> epicsFromHttpApi = gson.fromJson(jsonArray, new EpicListTypeToken().getType());
        assertArrayEquals(manager.getTasksByType(Epic.class).toArray(), epicsFromHttpApi.toArray());
    }

    @Test
    public void testDeleteEpicsById() throws IOException, InterruptedException {
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        manager.addTask(epic);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Epic> epicsFromHttpApi = gson.fromJson(jsonArray, new EpicListTypeToken().getType());
        assertEquals(1, epicsFromHttpApi.size());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .build();

        response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        jsonElement = JsonParser.parseString(response.body());
        jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonArray.isEmpty());
        assertTrue(manager.getTasksByType(Epic.class).isEmpty());
    }
}

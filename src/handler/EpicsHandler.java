package handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import manager.TaskManager;
import task.Epic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndPoint endPoint = getEndPoint(exchange.getRequestURI().getPath(),
                HttpMethod.valueOf(exchange.getRequestMethod()));

        try {
            switch (endPoint) {
                case GET_EPICS -> handleGetEpics(exchange);
                case GET_EPICS_BY_ID -> handleGetEpicsById(exchange, getIdFromPath(exchange));
                case GET_EPICS_SUBTASKS_BY_ID -> handleGetEpicSubtasksById(exchange, getIdFromPath(exchange));
                case POST_EPICS -> handlePostEpics(exchange);
                case DELETE_EPICS_BY_ID -> handleDeleteEpicsById(exchange, getIdFromPath(exchange));
                default -> handleIncorrectRequest(exchange);
            }
        } catch (IOException e) {
            sendError(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getTasksByType(Epic.class)), 200);
    }

    private void handleGetEpicsById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null && taskManager.getTaskById(id).getClass() == Epic.class) {
            sendText(exchange, gson.toJson(taskManager.getTaskById(id)), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubtasksById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null && taskManager.getTaskById(id).getClass() == Epic.class) {
            sendText(exchange, gson.toJson(taskManager.getSubtasks(id)), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpics(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(body);
        if(!jsonElement.isJsonObject()) {
            sendText(exchange, "Не удается обработать запрос. Проверьте тело запроса", 400);
            return;
        }
        Epic epic = gson.fromJson(body, Epic.class);

        boolean gotId = epic.getId() != null;

        Boolean hasDateIntersect = hasDatesIntersect(epic);
        if (hasDateIntersect != null && !hasDateIntersect) {
            if (gotId) {
                taskManager.updateTask(epic);
            } else {
                taskManager.addTask(epic);
            }
            if (taskManager.getAllTasks().contains(epic)) {
                sendText(exchange, "Эпик успешно добавлен/обновлен", 201);
            } else {
                sendText(exchange, "Эпик не добавлен. Проверьте корректность тела запроса", 400);
            }
        } else {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteEpicsById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null) {
            taskManager.deleteTaskById(id);
            sendText(exchange, String.format("Эпик id:%d был удален", id), 200);
        } else {
            sendNotFound(exchange);
        }
    }


    @Override
    protected EndPoint getEndPoint(String requestPath, HttpMethod method) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length > 3) {
            try {
                Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                return EndPoint.UNKNOWN;
            }
        }

        if (pathParts[1].equals("epics")) {
            if (pathParts.length == 2) {
                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_EPICS;
                } else if (HttpMethod.POST.equals(method)) {
                    return EndPoint.POST_EPICS;
                }
            } else if (pathParts.length == 3) {
                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_EPICS_BY_ID;
                }
                if (HttpMethod.DELETE.equals(method)) {
                    return EndPoint.DELETE_EPICS_BY_ID;
                }
            } else if (pathParts.length == 4) {
                if (pathParts[3].equals("subtasks") && HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_EPICS_SUBTASKS_BY_ID;
                }
            }
        }
        return EndPoint.UNKNOWN;
    }
}

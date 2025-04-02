package handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import manager.TaskManager;
import task.Subtask;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndPoint endPoint = getEndPoint(exchange.getRequestURI().getPath(),
                HttpMethod.valueOf(exchange.getRequestMethod()));

        try {
            switch (endPoint) {
                case GET_SUBTASKS: {
                    handleGetSubtasks(exchange);
                    break;
                }
                case GET_SUBTASKS_BY_ID: {
                    handleGetSubtasksById(exchange, getIdFromPath(exchange));
                    break;
                }
                case POST_SUBTASKS: {
                    handlePostSubtasks(exchange);
                    break;
                }
                case DELETE_SUBTASKS_BY_ID: {
                    handleDeleteSubtasksById(exchange, getIdFromPath(exchange));
                    break;
                }
                default:
                    handleIncorrectRequest(exchange);
            }
        } catch (IOException e) {
            sendError(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getTasksByType(Subtask.class)), 200);
    }

    private void handleGetSubtasksById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null && taskManager.getTaskById(id).getClass() == Subtask.class) {
            sendText(exchange, gson.toJson(taskManager.getTaskById(id)), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostSubtasks(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(body);
        if(!jsonElement.isJsonObject()) {
            sendText(exchange, "Не удается обработать запрос. Проверьте тело запроса", 400);
            return;
        }
        Subtask subtask = gson.fromJson(body, Subtask.class);
        boolean gotId = subtask.getId() != null;

        Boolean hasDateIntersect = hasDatesIntersect(subtask);
        if (hasDateIntersect != null && !hasDateIntersect) {
            if (gotId) {
                taskManager.updateTask(subtask);
            } else {
                taskManager.addTask(subtask);
            }
            if (taskManager.getAllTasks().contains(subtask)) {
                sendText(exchange, "Подзадача успешно добавлена/обновлена", 201);
            } else {
                sendText(exchange, "Подзадача не добавлена. Проверьте корректность тела запроса", 400);
            }
        } else {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteSubtasksById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null) {
            taskManager.deleteTaskById(id);
            sendText(exchange, String.format("Подзадача id:%d была удалена", id), 200);
        } else {
            sendNotFound(exchange);
        }
    }



    @Override
    protected EndPoint getEndPoint(String requestPath, HttpMethod method) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("subtasks")) {
            if (pathParts.length == 2) {
                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_SUBTASKS;
                } else if (HttpMethod.POST.equals(method)) {
                    return EndPoint.POST_SUBTASKS;
                }
            } else if (pathParts.length == 3) {
                try {
                    Integer.parseInt(pathParts[2]);
                } catch (NumberFormatException e) {
                    return EndPoint.UNKNOWN;
                }

                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_SUBTASKS_BY_ID;
                }
                if (HttpMethod.DELETE.equals(method)) {
                    return EndPoint.DELETE_SUBTASKS_BY_ID;
                }
            }
        }
        return EndPoint.UNKNOWN;
    }
}

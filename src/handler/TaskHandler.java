package handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import manager.TaskManager;
import task.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndPoint endPoint = getEndPoint(exchange.getRequestURI().getPath(),
                HttpMethod.valueOf(exchange.getRequestMethod()));

        try {
            switch (endPoint) {
                case GET_TASKS: {
                    handleGetTasks(exchange);
                    break;
                }
                case GET_TASKS_BY_ID: {
                    handleGetTasksById(exchange, getIdFromPath(exchange));
                    break;
                }
                case POST_TASKS: {
                    handlePostTasks(exchange);
                    break;
                }
                case DELETE_TASKS_BY_ID: {
                    handleDeleteTasksById(exchange, getIdFromPath(exchange));
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

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getTasksByType(Task.class)), 200);
    }

    private void handleGetTasksById(HttpExchange exchange, int id) throws IOException  {
        if (taskManager.getTaskById(id) != null && taskManager.getTaskById(id).getClass() == Task.class) {
            sendText(exchange, gson.toJson(taskManager.getTaskById(id)), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostTasks(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(body);
        if(!jsonElement.isJsonObject()) {
            sendText(exchange, "Не удается обработать запрос. Проверьте тело запроса", 400);
            return;
        }
        Task task = gson.fromJson(body, Task.class);
        boolean gotId = task.getId() != null;

        Boolean hasDateIntersect = hasDatesIntersect(task);
        if (hasDateIntersect != null && !hasDateIntersect) {
            if (gotId) {
                taskManager.updateTask(task);
            } else {
                taskManager.addTask(task);
            }
            if (taskManager.getAllTasks().contains(task)) {
                sendText(exchange, "Задача успешно добавлена/обновлена", 201);
            } else {
                sendText(exchange, "Задача не добавлена. Проверьте корректность тела запроса", 400);
            }
        } else {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTasksById(HttpExchange exchange, int id) throws IOException {
        if (taskManager.getTaskById(id) != null) {
            taskManager.deleteTaskById(id);
            sendText(exchange, String.format("Задача id:%d была удалена", id), 200);
        } else {
            sendNotFound(exchange);
        }
    }

    @Override
    protected EndPoint getEndPoint(String requestPath, HttpMethod method) {
        String[] pathParts = requestPath.split("/");

        if (pathParts[1].equals("tasks")) {
            if (pathParts.length == 2) {
                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_TASKS;
                } else if (HttpMethod.POST.equals(method)) {
                    return EndPoint.POST_TASKS;
                }
            } else if (pathParts.length == 3) {
                try {
                    Integer.parseInt(pathParts[2]);
                } catch (NumberFormatException e) {
                    return EndPoint.UNKNOWN;
                }
                if (HttpMethod.GET.equals(method)) {
                    return EndPoint.GET_TASKS_BY_ID;
                }
                if (HttpMethod.DELETE.equals(method)) {
                    return EndPoint.DELETE_TASKS_BY_ID;
                }
            }
        }
        return EndPoint.UNKNOWN;
    }
}



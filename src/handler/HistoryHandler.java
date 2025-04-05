package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import java.io.IOException;
import java.util.Objects;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndPoint endPoint = getEndPoint(exchange.getRequestURI().getPath(),
                HttpMethod.valueOf(exchange.getRequestMethod()));

        try {
            if (Objects.requireNonNull(endPoint) == EndPoint.GET_HISTORY) {
                handleGetHistory(exchange);
            } else {
                handleIncorrectRequest(exchange);
            }
        } catch (IOException e) {
            sendError(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
    }

    @Override
    protected EndPoint getEndPoint(String requestPath, HttpMethod method) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("history")) {
            if (HttpMethod.GET.equals(method)) {
                return EndPoint.GET_HISTORY;
            }
        }
        return EndPoint.UNKNOWN;
    }
}

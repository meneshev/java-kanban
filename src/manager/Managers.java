package manager;

import task.TaskManager;
import java.nio.file.Path;

public final class Managers {
    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    private Managers() {
    }

    public static TaskManager getDefault() {
        if (taskManager == null) {
            taskManager = new InMemoryTaskManager();
        }
        return taskManager;
    }

    public static TaskManager getCustomFileBacketTaskManager(Path file) {
        if (taskManager == null) {
            taskManager = new FileBackedTaskManager(file);
        }
        return taskManager;
    }

    public static TaskManager getFileBacketTaskManager() {
        if (taskManager == null) {
            taskManager = new FileBackedTaskManager();
        }
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        if (historyManager == null) {
            historyManager = new InMemoryHistoryManager();
        }
        return historyManager;
    }
}

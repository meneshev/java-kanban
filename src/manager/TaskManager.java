package manager;

import task.Task;
import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();

    void clearTasks();

    Task getTaskById(Integer id);

    void addTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(Integer id);

    List<Task> getTasksByType(Class<?> cl);

    void deleteTasksByType(Class<?> cl);

    List<Task> getSubtasks(int epicId);

    void printAllTasks();

    List<? extends Task> getHistory();
}

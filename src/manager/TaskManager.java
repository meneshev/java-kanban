package manager;

import task.Task;

import java.util.List;
import java.util.Map;

public interface TaskManager {
    Map<Integer, Task> getAllTasks();

    void clearTasks();

    Task getTaskById(Integer id);

    void addTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(Integer id);

    Map<Integer, Task> getTasksByType(Class<?> cl);

    List<Task> getSubtasks(int epicId);

    void printAllTasks();

    List<? extends Task> getHistory();
}

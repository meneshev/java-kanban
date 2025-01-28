import java.util.List;
import java.util.Map;

public interface TaskManager {
    Map<Integer, Task> getAllTasks();

    void clearTasks();

    Task getTaskById(Integer id) throws CloneNotSupportedException;

    void addTask(Task task);

    void updateTask(Task task) throws CloneNotSupportedException;

    void deleteTaskById(Integer id);

    Map<Integer, Task> getTasksByType(Class<?> cl);

    Map<Integer, Task> getSubtasks(int epicId);

    void printAllTasks();

    List<? extends Task> getHistory();
}

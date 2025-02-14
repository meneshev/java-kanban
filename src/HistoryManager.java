import java.util.List;

public interface HistoryManager {
    void add(Task task) throws CloneNotSupportedException;

    List<Task> getHistory();

    void clearHistory();

    void remove(Integer id);
}

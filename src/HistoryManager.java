import java.util.ArrayList;

public interface HistoryManager {
    void add(Task task) throws CloneNotSupportedException;

    ArrayList<Task> getHistory();

    void clearHistory();
}

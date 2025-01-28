import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> viewedTasks = new ArrayList<>(10);

    @Override
    public void add(Task task) throws CloneNotSupportedException {
        if (Objects.isNull(task)) {
            return;
        }
        viewedTasks.add(task.clone());
        if (viewedTasks.size() > 10) {
            viewedTasks.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return viewedTasks;
    }

    @Override
    public void clearHistory() {
        viewedTasks.clear();
    }
}

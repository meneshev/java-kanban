package manager;

import task.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> viewedTasks = new ArrayList<>(10);

    @Override
    public void add(Task task) {
        try {
            if (Objects.isNull(task)) {
                return;
            }
            viewedTasks.add(task.clone());
            if (viewedTasks.size() > 10) {
                viewedTasks.removeFirst();
            }
        } catch (CloneNotSupportedException e) {
            System.out.printf("ERROR: не удалось добавить задачу в историю. Ошибка: %s\n", e.getMessage());
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

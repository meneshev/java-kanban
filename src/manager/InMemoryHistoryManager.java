package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> firstElement;
    private Node<Task> lastElement;
    public final HashMap<Integer, Node<Task>> historyMap = new HashMap<>();

    public void add(Task task)  {
        if (Objects.isNull(task)) {
            return;
        }
        try {
            Task clonedTask = task.clone();
            linkLast(clonedTask, historyMap.containsKey(clonedTask.getId()));
            historyMap.put(task.getId(), lastElement);
        } catch (CloneNotSupportedException e) {
            System.out.println("WARN: Ошибка при добавлении задачи в историю");
        }
    }

    private void linkLast(Task task, boolean nodeAlreadyExist) {
        if (nodeAlreadyExist) {
            removeNode(historyMap.get(task.getId()));
        }

        Node<Task> last = lastElement;
        Node<Task> newElement = new Node<>(last, task, null);
        lastElement = newElement;

        if (last == null) {
            firstElement = newElement;
        } else {
            last.next = newElement;
        }
    }

    private void removeNode(Node<Task> el) {
        Node<Task> previousEl = el.previous;
        Node<Task> nextEl = el.next;

        if (previousEl == null) {
            firstElement = nextEl;
        } else {
            previousEl.next = nextEl;
            el.previous = null;
        }

        if (nextEl == null) {
            lastElement = previousEl;
        } else {
            nextEl.previous = previousEl;
            el.next = null;
        }

        el.data = null;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> current = firstElement;
        while (current != null) {
            try {
                tasks.add(current.data.clone());
            } catch (CloneNotSupportedException e) {
                System.out.println("WARN: Ошибка при клонировании задачи");
            }
            current = current.next;
        }
        return tasks;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void clearHistory() {
        for (Node<Task> x = firstElement; x != null; ) {
            Node<Task> next = x.next;
            x.data = null;
            x.next = null;
            x.previous = null;
            x = next;
        }
        firstElement = lastElement = null;
        historyMap.clear();
    }

    @Override
    public void remove(Integer id) {
        if (id == null || !historyMap.containsKey(id)) return;
        Task deletedTask = historyMap.get(id).data;
        // при удалении эпика, сначала удаляем его подзадачи
        if (deletedTask.getClass() == Epic.class) {
            List<Integer> subtaskIds = new ArrayList<>();
            for (Node<Task> t : historyMap.values()) {
                if (t.data.getClass() == Subtask.class) {
                    Subtask subtask = (Subtask) t.data;
                    if (subtask.getEpicId().equals(deletedTask.getId())) {
                        subtaskIds.add(subtask.getId());
                    }
                }
            }
            if (!subtaskIds.isEmpty()) {
                subtaskIds.forEach(el -> removeNode(historyMap.get(el)));
                subtaskIds.forEach(el -> historyMap.remove(el));
            }
        }
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }
}


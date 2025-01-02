import java.util.HashMap;
import java.util.Map;

public class TaskManager {
    private static Integer counter = 0;
    private final Map<Integer, Task> taskMap;

    public TaskManager() {
        this.taskMap = new HashMap<>();
    }

    public Map<Integer, Task> getAllTasks() {
        return taskMap;
    }

    public void clearTasks() {
        System.out.println("INFO: Полное удаление списка задач");
        taskMap.clear();
    }

    public Task getTaskById(Integer id) {
        return taskMap.get(id);
    }

    public void addTask(Task task) {
        int id = getNewTaskId();
        task.setId(id);
        taskMap.put(id, task);
        System.out.println("INFO: Добавлена новая задача с идентификатором " + id);
    }

    public void updateTask(Task task) {
        taskMap.put(task.getId(), task);

        if (task.getClass() == Subtask.class) {
            int epicId = ((Subtask) task).getEpicId();
            Epic epic = (Epic) getTaskById(epicId);
            calculateEpicStatus(epic);
        }
        System.out.println("INFO: Обновлена задача с идентификатором " + task.getId());
    }

    public void deleteTaskById(Integer id) {
        Task task = getTaskById(id);
        if (task == null) {
            System.out.println("WARN: Задача с идентификатором " + id + " не найдена");
            return;
        }
        if (task.getClass() == Epic.class) {
            Map<Integer, Task> subtasks = getSubtasks(id);
            if (!subtasks.isEmpty()) {
                System.out.println("WARN: Невозможно удалить эпик с идентификатором " + id
                        + ". На эпик ссылаются подзадачи");
                return;
            }
        }
        taskMap.remove(id);
        System.out.println("INFO: Задача с идентификатором " + id + " была удалена");
    }

    public void calculateEpicStatus(Task task) {
        Map<Integer, Task> subtasks = getSubtasks(task.getId());
        if (subtasks.isEmpty()) {
            ((Epic) task).setStatus(TaskStatus.NEW, true);
            System.out.println("INFO: Статус эпика с идентификатором " + task.getId() + " изменен на NEW");
            return;
        }

        boolean allSubtasksIsDone = true;
        for (Task t : subtasks.values()) {
            if (t.getStatus() != TaskStatus.DONE) {
                allSubtasksIsDone = false;
                break;
            }
        }
        if (allSubtasksIsDone) {
            ((Epic) task).setStatus(TaskStatus.DONE, true);
            System.out.println("INFO: Статус эпика с идентификатором " + task.getId() + " изменен на DONE");
            return;
        }

        ((Epic) task).setStatus(TaskStatus.IN_PROGRESS, true);
        System.out.println("INFO: Статус эпика с идентификатором " + task.getId() + " изменен на IN_PROGRESS");
    }

    public Map<Integer, Task> getTasksByType(Class<?> cl) {
        Map<Integer, Task> tasks = new HashMap<>();
        for (Task task : taskMap.values()) {
            if (task.getClass() == cl) {
                tasks.put(task.getId(), task);
            }
        }
        return tasks;
    }

    public Map<Integer, Task> getSubtasks(int epicId) {
        Map<Integer, Task> subtasks = new HashMap<>();
        for (Task t : getTasksByType(Subtask.class).values()) {
            if (((Subtask) t).getEpicId() == epicId) {
                subtasks.put(t.getId(), t);
            }
        }
        return subtasks;
    }

    public static Integer getNewTaskId() {
        return ++counter;
    }

    public static Integer getLastTaskId() {
        return counter;
    }

    public void printAllTasks() {
        System.out.println("*****");
        System.out.println("Задачи:");
        for (Task t : getTasksByType(Task.class).values()) {
            System.out.println(t);
        }
        System.out.println("Эпики:");
        for (Task t : getTasksByType(Epic.class).values()) {
            System.out.println(t);
        }
        System.out.println("Подзадачи:");
        for (Task t : getTasksByType(Subtask.class).values()) {
            System.out.println(t);
        }
        System.out.println("*****");
    }
}

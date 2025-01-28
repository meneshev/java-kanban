import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static Integer counter = 0;
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public Map<Integer, Task> getAllTasks() {
        return taskMap;
    }

    @Override
    public void clearTasks() {
        System.out.println("INFO: Полное удаление списка задач");
        taskMap.clear();
        counter = 0;
    }

    public void clearHistory() {
        historyManager.clearHistory();
    }

    //в историю просмотра записываются только вызовы извне по идентификатору
    @Override
    public Task getTaskById(Integer id) throws CloneNotSupportedException {
        if (taskMap.containsKey(id)) {
            addToViewed(taskMap.get(id));
        }
        return taskMap.get(id);
    }

    //метод для внутреннего использования, в нем не обновляется информация о просмотре задач
    private Task getTaskById(Integer id, Boolean innerUse) {
        if (innerUse) {
            return taskMap.get(id);
        } else {
            return null;
        }
    }

    @Override
    public void addTask(Task task) {
        if (validateTask(task)) {
            if (task.getId() != null) {
                if (taskMap.containsKey(task.getId())) {
                    updateTask(task, true);
                }
            } else {
                int id = getNewTaskId();
                task.setId(id);
                taskMap.put(id, task);
                System.out.println("INFO: Добавлена новая задача с идентификатором " + id);
            }
        } else {
            System.out.println("WARN: Задача не прошла валидацию и не была добавлена");
        }

    }

    private Boolean validateTask(Task task) {
        if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            Map<Integer, Task> epicsList = getTasksByType(Epic.class);
            if (!epicsList.containsKey(subtask.getEpicId())) {
                System.out.println("WARN: Подзадача должна ссылаться на эпик");
                return false;
            }
        }
        return true;
    }

    private void updateTask(Task task, Boolean isValid) {
        if (isValid) {
            taskMap.put(task.getId(), task);

            if (task.getClass() == Subtask.class) {
                int epicId = ((Subtask) task).getEpicId();
                Epic epic = (Epic) getTaskById(epicId, true);
                calculateEpicStatus(epic);
            }
            System.out.println("INFO: Обновлена задача с идентификатором " + task.getId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (validateTask(task)) {
            taskMap.put(task.getId(), task);

            if (task.getClass() == Subtask.class) {
                int epicId = ((Subtask) task).getEpicId();
                Epic epic = (Epic) getTaskById(epicId, true);
                calculateEpicStatus(epic);
            }
            System.out.println("INFO: Обновлена задача с идентификатором " + task.getId());
        } else {
            System.out.println("WARN: Задача не прошла валидацию и не была добавлена");
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        Task task = getTaskById(id, true);
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

    @Override
    public Map<Integer, Task> getTasksByType(Class<?> cl) {
        Map<Integer, Task> tasks = new HashMap<>();
        for (Task task : taskMap.values()) {
            if (task.getClass() == cl) {
                tasks.put(task.getId(), task);
            }
        }
        return tasks;
    }

    @Override
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

    @Override
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addToViewed(Task task) throws CloneNotSupportedException {
        historyManager.add(task);
    }
}

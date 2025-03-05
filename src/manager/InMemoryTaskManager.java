package manager;

import task.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected static Integer counter = 0;
    protected Map<Integer, Task> taskMap = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    public InMemoryTaskManager() {
    }

    public InMemoryTaskManager(Map<Integer, Task> taskMap) {
        this.taskMap = taskMap;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
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
    public Task getTaskById(Integer id) {
        if (taskMap.containsKey(id)) {
            addToViewed(taskMap.get(id));
        }
        return taskMap.get(id);
    }

    //метод для внутреннего использования, в нем не обновляется информация о просмотре задач
    protected Task getTaskById(Integer id, Boolean innerUse) {
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
                calculateEpicStatus(task);
                System.out.println("\nINFO: Добавлена новая задача с идентификатором " + id);
            }
        } else {
            System.out.println("WARN: Задача не прошла валидацию и не была добавлена");
        }
    }

    protected Boolean validateTask(Task task) {
        if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            List<Task> epicsList = getTasksByType(Epic.class);
            boolean hasEpic = false;
            for (Task t : epicsList) {
                if (t.getId() == subtask.getEpicId()) {
                    hasEpic = true;
                    break;
                }
            }
            if (!hasEpic) {
                System.out.println("WARN: Подзадача должна ссылаться на эпик");
                return false;
            }
        }
        return true;
    }

    protected void updateTask(Task task, Boolean isValid) {
        if (isValid) {
            taskMap.put(task.getId(), task);
            calculateEpicStatus(task);
            System.out.println("INFO: Обновлена задача с идентификатором " + task.getId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (validateTask(task)) {
            taskMap.put(task.getId(), task);
            calculateEpicStatus(task);
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
            List<Task> subtasks = getSubtasks(id);
            System.out.println("INFO: Удаление подзадач эпика с идентификатором " + id);
            for (Task subtask : subtasks) {
                taskMap.remove(subtask.getId());
            }
            taskMap.remove(id);
        } else {
            taskMap.remove(id);
            calculateEpicStatus(task);
        }
        System.out.println("INFO: Задача с идентификатором " + id + " была удалена");
    }

    public void calculateEpicStatus(Task task) {
        if (task.getClass() != Subtask.class) {
            return;
        }

        Epic epic = (Epic) taskMap.get(((Subtask) task).getEpicId());
        List<Task> subtasks = getSubtasks(epic.getId());

        boolean allSubtasksIsDone = true;
        boolean allSubtasksIsNew = true;
        for (Task t : subtasks) {
            if (t.getStatus() != TaskStatus.DONE) {
                allSubtasksIsDone = false;
            }
            if (t.getStatus() != TaskStatus.NEW) {
                allSubtasksIsNew = false;
            }

            if (!allSubtasksIsDone && !allSubtasksIsNew) {
                break;
            }
        }

        if (subtasks.isEmpty() || allSubtasksIsNew) {
            epic.setStatus(TaskStatus.NEW, true);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на NEW");
        } else if (allSubtasksIsDone) {
            epic.setStatus(TaskStatus.DONE, true);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на DONE");
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS, true);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на IN_PROGRESS");
        }
    }

    @Override
    public List<Task> getTasksByType(Class<?> cl) {
        List<Task> tasks = new ArrayList<>();
        for (Task task : taskMap.values()) {
            if (task.getClass() == cl) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public void deleteTasksByType(Class<?> cl) {
        List<Task> tasksToDelete = new ArrayList<>();
        for (Task task : taskMap.values()) {
            if (task.getClass() == cl) {
                tasksToDelete.add(task);
            }
        }
        for (Task t : tasksToDelete) {
            deleteTaskById(t.getId());
        }
    }

    @Override
    public List<Task> getSubtasks(int epicId) {
        List<Task> subtasks = new ArrayList<>();
        for (Task t : getTasksByType(Subtask.class)) {
            if (((Subtask) t).getEpicId() == epicId) {
                subtasks.add(t);
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
        for (Task t : getTasksByType(Task.class)) {
            System.out.println(t);
        }
        System.out.println("Эпики:");
        for (Task t : getTasksByType(Epic.class)) {
            System.out.println(t);
        }
        System.out.println("Подзадачи:");
        for (Task t : getTasksByType(Subtask.class)) {
            System.out.println(t);
        }
        System.out.println("*****");
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void addToViewed(Task task) {
        historyManager.add(task);
    }
}

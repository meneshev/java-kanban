package manager;

import exception.NotFoundException;
import task.*;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected static Integer counter = 0;
    protected Map<Integer, Task> taskMap = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    public final Map<LocalDateTime, Task> prioritizedTasks = new TreeMap<>();

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
        prioritizedTasks.clear();
        clearHistory();
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
            return taskMap.get(id);
        } else {
            throw new NotFoundException("Объект не найден");
        }

    }

    //метод для внутреннего использования, в нем не обновляется информация о просмотре задач
    public Task getTaskById(Integer id, Boolean innerUse) {
        if (innerUse) {
            return taskMap.get(id);
        } else {
            throw new NotFoundException("Объект не найден");
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
                calculateEpicDates(task);
                addToPrioritized(task);
                System.out.println("\nINFO: Добавлена новая задача с идентификатором " + id);
            }
        } else {
            System.out.println("WARN: Задача не прошла валидацию и не была добавлена");
        }
    }

    public boolean hasDateIntersect(Task task) {
        if (task.getStartTime().isPresent()) {
            if (task.getId() != null) {
                Task sameTaskFromPrioritized = prioritizedTasks.get(task.getStartTime().get());
                if (sameTaskFromPrioritized != null
                        && sameTaskFromPrioritized.getStartTime().get().equals(task.getStartTime().get())
                        && sameTaskFromPrioritized.getDuration().equals(task.getDuration())) {
                    return false;
                }
            }
            return prioritizedTasks.values().stream()
                    .anyMatch(prioritizedTask -> (task.getStartTime().get().isBefore(prioritizedTask.getStartTime().get()) &&
                            task.getEndTime().get().isAfter(prioritizedTask.getStartTime().get())) ||
                            (task.getStartTime().get().isBefore(prioritizedTask.getEndTime().get())));
        }
        return false;
    }

    protected void addToPrioritized(Task task) {
        if (task.getStartTime().isEmpty() || task.getClass() == Epic.class) {
            return;
        }

        if (prioritizedTasks.containsKey(task.getStartTime().get()) && task.getStatus().equals(TaskStatus.DONE)) {
            prioritizedTasks.remove(task.getStartTime().get());
            return;
        }

        boolean isDateIntersect = hasDateIntersect(task);

        if (!isDateIntersect) {
            prioritizedTasks.put(task.getStartTime().get(), task);
        }
    }

    protected Boolean validateTask(Task task) {
        if (task.getClass() == Subtask.class) {
            boolean hasEpic = taskMap.values().stream()
                    .filter(epic -> epic.getClass() == Epic.class)
                    .anyMatch(epic -> Objects.equals(epic.getId(), ((Subtask) task).getEpicId()));
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
            calculateEpicDates(task);
            addToPrioritized(task);
            System.out.println("INFO: Обновлена задача с идентификатором " + task.getId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (validateTask(task)) {
            taskMap.put(task.getId(), task);
            calculateEpicStatus(task);
            calculateEpicDates(task);
            addToPrioritized(task);
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
            System.out.println("INFO: Удаление подзадач эпика с идентификатором " + id);
            getSubtasks(id).forEach(subtask -> taskMap.remove(subtask.getId()));
            taskMap.remove(id);

        } else {
            taskMap.remove(id);
            calculateEpicStatus(task);
            calculateEpicStatus(task);
        }
        if (task.getStartTime().isPresent()) {
            prioritizedTasks.remove(task.getStartTime().get());
        }
        System.out.println("INFO: Задача с идентификатором " + id + " была удалена");
    }

    private void calculateEpicStatus(Task task) {
        if (task.getClass() != Subtask.class) {
            return;
        }

        Epic epic = (Epic) taskMap.get(((Subtask) task).getEpicId());

        boolean allSubtasksIsDone = getSubtasks(epic.getId()).stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);

        boolean allSubtasksIsNew = getSubtasks(epic.getId()).stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW);

        if (allSubtasksIsNew) {
            epic.setStatusForce(TaskStatus.NEW);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на NEW");
        } else if (allSubtasksIsDone) {
            epic.setStatusForce(TaskStatus.DONE);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на DONE");
        } else {
            epic.setStatusForce(TaskStatus.IN_PROGRESS);
            System.out.println("INFO: Статус эпика с идентификатором " + epic.getId() + " изменен на IN_PROGRESS");
        }
    }

    private void calculateEpicDates(Task task) {
        if (task.getClass() != Subtask.class) {
            return;
        }
        Epic epic = (Epic) taskMap.get(((Subtask) task).getEpicId());

        epic.setStartTime(
                getSubtasks(epic.getId()).stream()
                        .map(Task::getStartTime)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .min(LocalDateTime::compareTo)
                        .orElse(null)
        );

        epic.setDuration(
                getSubtasks(epic.getId()).stream()
                        .map(subtask -> subtask.getDuration().toMinutes())
                        .reduce(0L, Long::sum)
        );

        epic.setEndTime(
                getSubtasks(epic.getId()).stream()
                        .map(Task::getEndTime)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .max(LocalDateTime::compareTo)
                        .orElse(null)
        );
    }

    @Override
    public List<Task> getTasksByType(Class<?> cl) {
        return taskMap.values().stream()
                .filter(task -> task.getClass() == cl)
                .toList();
    }

    @Override
    public void deleteTasksByType(Class<?> cl) {
        List<Task> tasksToDelete = taskMap.values().stream()
                .filter(cl::isInstance).toList();
        tasksToDelete.forEach(task -> deleteTaskById(task.getId()));
    }

    @Override
    public List<Subtask> getSubtasks(int epicId) {
        return taskMap.values().stream()
                .filter(Subtask.class::isInstance)
                .map(Subtask.class::cast)
                .filter(subtask -> subtask.getEpicId() == epicId)
                .toList();
    }


    public static Integer getNewTaskId() {
        return ++counter;
    }

    public static Integer getLastTaskId() {
        return counter;
    }

    @Override
    public void printAllTasks() {
        System.out.println("Задачи:");
        taskMap.values().stream()
                .filter(task -> task.getClass() == Task.class)
                .forEach(System.out::println);

        System.out.println("Эпики:");
        taskMap.values().stream()
                .filter(Epic.class::isInstance)
                .forEach(System.out::println);

        System.out.println("Подзадачи:");
        taskMap.values().stream()
                .filter(Subtask.class::isInstance)
                .forEach(System.out::println);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void addToViewed(Task task) {
        historyManager.add(task);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks.values());
    }
}

package manager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import util.ObjectBuilder;
import java.util.ArrayList;
import java.util.List;

class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void prepareTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void viewAddToHistory() {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        assertTrue(taskManager.getHistory().contains(task));
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    public void commonRemove() {
        InMemoryHistoryManager hm = (InMemoryHistoryManager) taskManager.historyManager;

        // удаление на пустой истории
        hm.remove(100);

        for (int i = 0; i < 17; i++) {
            taskManager.addTask(ObjectBuilder.of(Task::new)
                    .with(Task::setName, "Some name")
                    .with(Task::setDescription, "Some description")
                    .with(Task::setDuration, 30L)
                    .build());
        }

        taskManager.getAllTasks().forEach(task -> taskManager.getTaskById(task.getId()));


        int historySize = hm.getHistory().size();

        // удалим 1-ый элемент, ожидаем что у 2-го элемента prev станет null
        Node<Task> nextDeleted = hm.historyMap.get(2);
        Node<Task> deleted = hm.historyMap.get(1);
        assertEquals(nextDeleted.previous, deleted);
        hm.remove(1);
        assertNull(nextDeleted.previous);
        assertEquals(historySize - 1, hm.historyMap.size());
        historySize = hm.historyMap.size();


        // удалим элемент из середины (10-ый), ожидаем что у предыдущего элемента next станет next удаляемого,
        // у след. элемента prev станет prev удаляемого
        Node<Task> beforeDeleted = hm.historyMap.get(9);
        deleted = hm.historyMap.get(10);
        nextDeleted = hm.historyMap.get(11);
        assertEquals(nextDeleted.previous, deleted);
        assertEquals(beforeDeleted.next, deleted);
        hm.remove(10);
        assertEquals(nextDeleted.previous, beforeDeleted);
        assertEquals(beforeDeleted.next, nextDeleted);
        assertEquals(historySize - 1, hm.historyMap.size());
        historySize = hm.historyMap.size();

        // удалим последний элемент, ожидаем что у предпоследнего элемента next станет null
        beforeDeleted = hm.historyMap.get(16);
        deleted = hm.historyMap.get(17);
        assertEquals(beforeDeleted.next, deleted);
        hm.remove(17);
        assertNull(beforeDeleted.next);
        assertEquals(historySize - 1, hm.historyMap.size());
    }

    @Test
    public void epicRemove() {
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        Integer epic1Id = InMemoryTaskManager.getLastTaskId();
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        Subtask subtask3 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);

        taskManager.getAllTasks().forEach(task -> taskManager.getTaskById(task.getId()));

        InMemoryHistoryManager hm = (InMemoryHistoryManager) taskManager.historyManager;
        int historySize = hm.historyMap.size();
        assertEquals(4, historySize);

        hm.remove(epic1.getId());
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    public void historyContainsOnlyUniqueTasks() {
        for (int i = 0; i < 17; i++) {
            taskManager.addTask(ObjectBuilder.of(Task::new)
                    .with(Task::setName, "Some name")
                    .with(Task::setDescription, "Some description")
                    .with(Task::setDuration, 30L)
                    .build());
        }
        for (int i = 0; i < 3; i++) {
            taskManager.getAllTasks().forEach(task -> taskManager.getTaskById(task.getId()));
        }
        assertArrayEquals(taskManager.getAllTasks().toArray(),
                taskManager.historyManager.getHistory().toArray());
    }

    @Test
    public void historyCanContainsMoreThan10LastViews() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);
        Task task2 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task2);
        Task task3 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task3);
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        Integer epic1Id = InMemoryTaskManager.getLastTaskId();
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        Subtask subtask3 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1Id)
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        for (int i = 0; i < 17; i++) {
            taskManager.addTask(ObjectBuilder.of(Task::new)
                    .with(Task::setName, "Some name")
                    .with(Task::setDescription, "Some description")
                    .with(Task::setDuration, 30L)
                    .build());
        }

        List<Task> views = new ArrayList<>();
        taskManager.getAllTasks().forEach(task -> views.add(taskManager.getTaskById(task.getId())));
        System.out.println(taskManager.getAllTasks().size());
        System.out.println(taskManager.getHistory().size());
        assertArrayEquals(taskManager.getHistory().toArray(), views.toArray());
    }

    @Test
    public void tasksInTaskManagerAreSnapshots() {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        task.setId(-1);
        task.setName("New task name");
        task.setDescription("New task description");
        task.setStatus(TaskStatus.DONE);
        Task taskFromHistory = taskManager.getHistory().getLast();
        assertNotEquals(taskFromHistory.getId(), task.getId());
        assertNotEquals(taskFromHistory.getName(), task.getName());
        assertNotEquals(taskFromHistory.getDescription(), task.getDescription());
        assertNotEquals(taskFromHistory.getStatus(), task.getStatus());

        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        epic.setId(-2);
        epic.setName("New epic name");
        epic.setDescription("New epic description");
        Epic epicFromHistory = (Epic) taskManager.getHistory().getLast();
        assertNotEquals(epicFromHistory.getId(), epic.getId());
        assertNotEquals(epicFromHistory.getName(), epic.getName());
        assertNotEquals(epicFromHistory.getDescription(), epic.getDescription());
        epic.setId(InMemoryTaskManager.getLastTaskId());

        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, InMemoryTaskManager.getLastTaskId())
                .build();
        taskManager.addTask(subtask);
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        subtask.setId(-3);
        subtask.setName("New subtask name");
        subtask.setDescription("New subtask description");
        subtask.setStatus(TaskStatus.DONE);
        subtask.setEpicId(-99);
        Subtask subtaskFromHistory = (Subtask) taskManager.getHistory().getLast();
        assertNotEquals(subtaskFromHistory.getId(), subtask.getId());
        assertNotEquals(subtaskFromHistory.getName(), subtask.getName());
        assertNotEquals(subtaskFromHistory.getDescription(), subtask.getDescription());
        assertNotEquals(subtaskFromHistory.getStatus(), subtask.getStatus());
        assertNotEquals(subtaskFromHistory.getEpicId(), subtask.getEpicId());
    }
}

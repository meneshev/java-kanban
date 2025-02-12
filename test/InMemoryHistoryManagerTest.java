import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;


class InMemoryHistoryManagerTest {
    private static InMemoryTaskManager taskManager;

    @BeforeAll
    public static void prepareTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @BeforeEach
    public void clearTasks() {
        taskManager.clearTasks();
        taskManager.clearHistory();
    }

    @Test
    public void viewAddToHistory() throws CloneNotSupportedException {
        Task task = new Task("Task name", "Task description");
        taskManager.addTask(task);
        taskManager.getTaskById(InMemoryTaskManager.getLastTaskId());
        assertTrue(taskManager.getHistory().contains(task));
    }

    @Test
    public void commonRemove() throws CloneNotSupportedException {
        for (int i = 0; i < 17; i++) {
            taskManager.addTask(new Task("Generated task " + (i + 1), "Descr"));
        }
        for (Task task : taskManager.getAllTasks().values()) {
            taskManager.getTaskById(task.getId());
        }
        InMemoryHistoryManager hm = (InMemoryHistoryManager) taskManager.getHistoryManager();
        int historySize = hm.getHistoryMap().size();

        // удалим 1-ый элемент, ожидаем что у 2-го элемента prev станет null
        Node<Task> nextDeleted = hm.getHistoryMap().get(2);
        Node<Task> deleted = hm.getHistoryMap().get(1);
        assertEquals(nextDeleted.previous, deleted);
        hm.remove(1);
        assertNull(nextDeleted.previous);
        assertEquals(historySize - 1, hm.getHistoryMap().size());
        historySize = hm.getHistoryMap().size();


        // удалим элемент из середины (10-ый), ожидаем что у предыдущего элемента next станет next удаляемого,
        // у след. элемента prev станет prev удаляемого
        Node<Task> beforeDeleted = hm.getHistoryMap().get(9);
        deleted = hm.getHistoryMap().get(10);
        nextDeleted = hm.getHistoryMap().get(11);
        assertEquals(nextDeleted.previous, deleted);
        assertEquals(beforeDeleted.next, deleted);
        hm.remove(10);
        assertEquals(nextDeleted.previous, beforeDeleted);
        assertEquals(beforeDeleted.next, nextDeleted);
        assertEquals(historySize - 1, hm.getHistoryMap().size());
        historySize = hm.getHistoryMap().size();

        // удалим последний элемент, ожидаем что у предпоследнего элемента next станет null
        beforeDeleted = hm.getHistoryMap().get(16);
        deleted = hm.getHistoryMap().get(17);
        assertEquals(beforeDeleted.next, deleted);
        hm.remove(17);
        assertNull(beforeDeleted.next);
        assertEquals(historySize - 1, hm.getHistoryMap().size());
    }

    @Test
    public void epicRemove() throws CloneNotSupportedException {
        Epic epic1 = new Epic("Epic1 name", "Epic1 description");
        taskManager.addTask(epic1);
        Integer epic1Id = InMemoryTaskManager.getLastTaskId();
        Subtask subtask1 = new Subtask("Subtask1 name", "Subtask1 description",
                epic1Id);
        Subtask subtask2 = new Subtask("Subtask2 name", "Subtask2 description",
                epic1Id);
        Subtask subtask3 = new Subtask("Subtask3 name", "Subtask3 description",
                epic1Id);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);

        for (Task task : taskManager.getAllTasks().values()) {
            taskManager.getTaskById(task.getId());
        }
        InMemoryHistoryManager hm = (InMemoryHistoryManager) taskManager.getHistoryManager();
        int historySize = hm.getHistoryMap().size();
        assertEquals(4, historySize);

        hm.remove(epic1.getId());
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    public void historyContainsOnlyUniqueTasks() throws CloneNotSupportedException {
        for (int i = 0; i < 17; i++) {
            taskManager.addTask(new Task("Generated task " + (i + 1), "Descr"));
        }
        for (int i = 0; i < 3; i++) {
            for (Task task : taskManager.getAllTasks().values()) {
                taskManager.getTaskById(task.getId());
            }
        }
        assertArrayEquals(taskManager.getAllTasks().values().toArray(),
                taskManager.getHistoryManager().getHistory().toArray());
    }

    @Test
    public void historyCanContainsMoreThan10LastViews() throws CloneNotSupportedException {
        Task task1 = new Task("Task1 name", "Task1 description");
        taskManager.addTask(task1);
        Task task2 = new Task("Task2 name", "Task2 description");
        taskManager.addTask(task2);
        Task task3 = new Task("Task3 name", "Task3 description");
        taskManager.addTask(task3);
        Epic epic1 = new Epic("Epic1 name", "Epic1 description");
        taskManager.addTask(epic1);
        Integer epic1Id = InMemoryTaskManager.getLastTaskId();
        Subtask subtask1 = new Subtask("Subtask1 name", "Subtask1 description",
                epic1Id);
        Subtask subtask2 = new Subtask("Subtask2 name", "Subtask2 description",
                epic1Id);
        Subtask subtask3 = new Subtask("Subtask3 name", "Subtask3 description",
                epic1Id);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        for (int i = 0; i < 17; i++) {
            taskManager.addTask(new Task("Generated task " + (i + 1), "Descr"));
        }

        List<Task> views = new ArrayList<>();
        for (Task task : taskManager.getAllTasks().values()) {
            views.add(taskManager.getTaskById(task.getId()));
        }
        System.out.println(taskManager.getAllTasks().size());
        System.out.println(taskManager.getHistory().size());
        assertArrayEquals(taskManager.getHistory().toArray(), views.toArray());
    }

    @Test
    public void tasksInTaskManagerAreSnapshots() throws CloneNotSupportedException {
        Task task = new Task("Task name", "Task description");
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

        Epic epic = new Epic("Epic name", "Epic description");
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

        Subtask subtask = new Subtask("Subtask name", "Subtask description",
                InMemoryTaskManager.getLastTaskId());
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

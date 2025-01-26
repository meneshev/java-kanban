import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Random;

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
    public void historyContains10LastViews() throws CloneNotSupportedException {
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

        ArrayList<Task> firstSevenViews = new ArrayList<>();
        for (Task task : taskManager.getAllTasks().values()) {
            firstSevenViews.add(taskManager.getTaskById(task.getId()));
        }
        assertArrayEquals(taskManager.getHistory().toArray(), firstSevenViews.toArray());

        ArrayList<Task> lastTenViews = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Integer randomId = random.nextInt(InMemoryTaskManager.getLastTaskId()) + 1;
            lastTenViews.add(taskManager.getTaskById(randomId));
        }
        assertArrayEquals(lastTenViews.toArray(), taskManager.getHistory().toArray());
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

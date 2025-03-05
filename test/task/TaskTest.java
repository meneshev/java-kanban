package task;

import static org.junit.jupiter.api.Assertions.*;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskTest {
    private static TaskManager taskManager;
    @BeforeAll
    public static void prepareTaskManager() {
        taskManager = Managers.getDefault();
    }

    @BeforeEach
    public void clearTasks() {
        taskManager.clearTasks();
    }

    @Test
    public void tasksAreEqualsIfTheyHaveTheSameId() {
        Task task1 = new Task("Some name", "Some description");
        Task task2 = new Task("Some name", "Some description");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        assertNotEquals(task1, task2);
        task2.setId(InMemoryTaskManager.getLastTaskId() - 1);
        assertEquals(task1, task2);
    }
}

package task;

import static org.junit.jupiter.api.Assertions.*;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EpicTest {
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
    public void epicsAreEqualsIfTheyHaveTheSameId() {
        Epic epic1 = new Epic("Some name", "Some description");
        Epic epic2 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        taskManager.addTask(epic2);
        assertNotEquals(epic1, epic2);
        epic2.setId(InMemoryTaskManager.getLastTaskId() - 1);
        assertEquals(epic1, epic2);
    }
}

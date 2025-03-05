package task;

import static org.junit.jupiter.api.Assertions.*;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubtaskTest {
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
    public void subtasksAreEqualsIfTheyHaveTheSameId() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        Subtask subtask2 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        assertNotEquals(subtask1, subtask2);
        subtask2.setId(InMemoryTaskManager.getLastTaskId() - 1);
        assertEquals(subtask1, subtask2);
    }
}

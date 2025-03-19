package task;

import static org.junit.jupiter.api.Assertions.*;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.ObjectBuilder;

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
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1.getId())
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1.getId())
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        assertNotEquals(subtask1, subtask2);
        subtask2.setId(InMemoryTaskManager.getLastTaskId() - 1);
        assertEquals(subtask1, subtask2);
    }
}

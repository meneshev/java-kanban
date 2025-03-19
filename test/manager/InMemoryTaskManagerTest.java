package manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import util.ObjectBuilder;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    
    @BeforeEach
    public void prepareTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void tasksWithGeneratedAndManualIdAreNotConflicted() {
        Task taskWithGeneratedId = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(taskWithGeneratedId);
        Task taskWithManualId = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some new name")
                .with(Task::setDescription, "Some new description")
                .with(Task::setDuration, 30L)
                .build();
        Integer lastId = InMemoryTaskManager.getLastTaskId();
        taskWithManualId.setId(lastId);
        taskManager.addTask(taskWithManualId);
        assertEquals("Some new name", taskManager.getTaskById(lastId).getName());
        assertEquals("Some new description", taskManager.getTaskById(lastId).getDescription());
    }

    @Test
    public void tasksAreNotChangeAfterAddToTaskManager() {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        String taskNameAfterCreate = task.getName();
        String taskDescriptionAfterCreate = task.getDescription();
        TaskStatus taskStatusAfterCreate = task.getStatus();
        taskManager.addTask(task);
        Integer lastId = InMemoryTaskManager.getLastTaskId();
        assertEquals(taskNameAfterCreate, taskManager.getTaskById(lastId).getName());
        assertEquals(taskDescriptionAfterCreate, taskManager.getTaskById(lastId).getDescription());
        assertEquals(taskStatusAfterCreate, taskManager.getTaskById(lastId).getStatus());

        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        String epicNameAfterCreate = epic.getName();
        String epicDescriptionAfterCreate = epic.getDescription();
        TaskStatus epicStatusAfterCreate = epic.getStatus();
        taskManager.addTask(epic);
        lastId = InMemoryTaskManager.getLastTaskId();
        assertEquals(epicNameAfterCreate, taskManager.getTaskById(lastId).getName());
        assertEquals(epicDescriptionAfterCreate, taskManager.getTaskById(lastId).getDescription());
        assertEquals(epicStatusAfterCreate, taskManager.getTaskById(lastId).getStatus());

        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, lastId)
                .build();
        String subtaskNameAfterCreate = subtask.getName();
        String subtaskDescriptionAfterCreate = subtask.getDescription();
        TaskStatus subtaskStatusAfterCreate = subtask.getStatus();
        Integer subtaskEpicId = subtask.getEpicId();
        taskManager.addTask(subtask);
        lastId = InMemoryTaskManager.getLastTaskId();
        Subtask subtaskFromManager = (Subtask) taskManager.getTaskById(lastId);
        assertEquals(subtaskNameAfterCreate, subtaskFromManager.getName());
        assertEquals(subtaskDescriptionAfterCreate, subtaskFromManager.getDescription());
        assertEquals(subtaskStatusAfterCreate, subtaskFromManager.getStatus());
        assertEquals(subtaskEpicId, subtaskFromManager.getEpicId());
    }
}

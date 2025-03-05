package manager;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest {
    private static InMemoryTaskManager taskManager;
    @BeforeAll
    public static void prepareTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @BeforeEach
    public void clearTasks() {
        taskManager.clearTasks();
    }

    @Test
    public void taskAddedSuccessfully() {
        Task task1 = new Task("Some name", "Some description");
        taskManager.addTask(task1);
        assertTrue(taskManager.getAllTasks().contains(task1));
    }

    @Test
    public void taskDeletedSuccessfully() {
        Task task1 = new Task("Some name", "Some description");
        taskManager.addTask(task1);
        assertTrue(taskManager.getAllTasks().contains(task1));
        taskManager.deleteTaskById(task1.getId());
        assertNull(taskManager.getTaskById(task1.getId()));
    }

    @Test
    public void taskStatusSuccessfullyChanged() {
        Task task1 = new Task("Some name", "Some description");
        taskManager.addTask(task1);
        task1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()).getStatus());
    }

    @Test
    public void epicAddedSuccessfully() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        assertTrue(taskManager.getAllTasks().contains(epic1));
    }

    @Test
    public void epicAndSubtasksDeletedSuccessfully() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        assertTrue(taskManager.getAllTasks().contains(epic1));
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        taskManager.deleteTaskById(epic1.getId());
        assertNull(taskManager.getTaskById(subtask1.getId()));
        assertNull(taskManager.getTaskById(epic1.getId()));
    }

    @Test
    public void epicStatusSuccessfullyChanged() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        assertEquals(TaskStatus.NEW, taskManager.getTaskById(epic1.getId()).getStatus());
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        Subtask subtask2 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskById(epic1.getId()).getStatus());
        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.NEW, taskManager.getTaskById(epic1.getId()).getStatus());
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask2);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(epic1.getId()).getStatus());
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getTaskById(epic1.getId()).getStatus());
    }

    @Test
    public void subtaskAddedSuccessfully() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        Subtask subtask2 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        assertTrue(taskManager.getAllTasks().contains(subtask1));
        assertTrue(taskManager.getAllTasks().contains(subtask2));
    }

    @Test
    public void subtaskDeletedSuccessfully() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        assertTrue(taskManager.getAllTasks().contains(subtask1));
        taskManager.deleteTaskById(subtask1.getId());
        assertNull(taskManager.getTaskById(subtask1.getId()));
    }

    @Test
    public void subtaskStatusSuccessfullyChanged() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()).getStatus());
    }

    @Test
    public void subtaskShouldNotAddIfEpicIdIsSubtask() {
        Epic epic1 = new Epic("Some name", "Some description");
        taskManager.addTask(epic1);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        taskManager.addTask(subtask1);
        Subtask subtask2 = new Subtask("Some name", "Some description", subtask1.getId());
        taskManager.addTask(subtask2);
        assertEquals(1, taskManager.getTasksByType(Subtask.class).size());
    }

    @Test
    public void inMemoryTaskManagerCanGetAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        Task task = new Task("Some name", "Some description");
        taskManager.addTask(task);
        allTasks.add(task);
        Epic epic = new Epic("Some name", "Some description");
        taskManager.addTask(epic);
        allTasks.add(epic);
        Subtask subtask = new Subtask("Some name", "Some description", epic.getId());
        taskManager.addTask(subtask);
        allTasks.add(subtask);
        List<Task> allTasksFromManager = new ArrayList<>(taskManager.getAllTasks());
        assertArrayEquals(allTasks.toArray(), allTasksFromManager.toArray());
    }

    @Test
    public void inMemoryTaskManagerCanGetAllEpicSubtasks() {
        List<Task> allSubtasks = new ArrayList<>();
        Epic epic = new Epic("Some name", "Some description");
        taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic.getId());
        Subtask subtask2 = new Subtask("Some name", "Some description", epic.getId());
        Subtask subtask3 = new Subtask("Some name", "Some description", epic.getId());
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        allSubtasks.add(subtask1);
        allSubtasks.add(subtask2);
        allSubtasks.add(subtask3);
        assertArrayEquals(allSubtasks.toArray(), taskManager.getSubtasks(epic.getId()).toArray());
    }

    @Test
    public void inMemoryTaskManagerCanClearAllTasks() {
        Epic epic = new Epic("Some name", "Some description");
        taskManager.addTask(epic);
        Subtask subtask1 = new Subtask("Some name", "Some description", epic.getId());
        Subtask subtask2 = new Subtask("Some name", "Some description", epic.getId());
        Subtask subtask3 = new Subtask("Some name", "Some description", epic.getId());
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        taskManager.clearTasks();
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    public void inMemoryTaskManagerCanAddAndFindTasks() {
        Task task = new Task("Some name", "Some description");
        taskManager.addTask(task);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));
    }

    @Test
    public void inMemoryTaskManagerCanAddAndFindEpics() {
        Epic epic = new Epic("Some name", "Some description");
        taskManager.addTask(epic);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));
    }

    @Test
    public void inMemoryTaskManagerCanAddAndFindSubtasks() {
        Epic epic = new Epic("Some name", "Some description");
        taskManager.addTask(epic);
        Subtask subtask = new Subtask("Some name", "Some description", epic.getId());
        taskManager.addTask(subtask);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));
    }

    @Test
    public void tasksWithGeneratedAndManualIdAreNotConflicted() {
        Task taskWithGeneratedId = new Task("Some name", "Some description");
        taskManager.addTask(taskWithGeneratedId);
        Task taskWithManualId = new Task("Some new name", "Some new description");
        Integer lastId = InMemoryTaskManager.getLastTaskId();
        taskWithManualId.setId(lastId);
        taskManager.addTask(taskWithManualId);
        assertEquals("Some new name", taskManager.getTaskById(lastId).getName());
        assertEquals("Some new description", taskManager.getTaskById(lastId).getDescription());
    }

    @Test
    public void tasksAreNotChangeAfterAddToTaskManager() {
        Task task = new Task("Some task name", "Some task description");
        String taskNameAfterCreate = task.getName();
        String taskDescriptionAfterCreate = task.getDescription();
        TaskStatus taskStatusAfterCreate = task.getStatus();
        taskManager.addTask(task);
        Integer lastId = InMemoryTaskManager.getLastTaskId();
        assertEquals(taskNameAfterCreate, taskManager.getTaskById(lastId).getName());
        assertEquals(taskDescriptionAfterCreate, taskManager.getTaskById(lastId).getDescription());
        assertEquals(taskStatusAfterCreate, taskManager.getTaskById(lastId).getStatus());

        Epic epic = new Epic("Some epic name", "Some epic description");
        String epicNameAfterCreate = epic.getName();
        String epicDescriptionAfterCreate = epic.getDescription();
        TaskStatus epicStatusAfterCreate = epic.getStatus();
        taskManager.addTask(epic);
        lastId = InMemoryTaskManager.getLastTaskId();
        assertEquals(epicNameAfterCreate, taskManager.getTaskById(lastId).getName());
        assertEquals(epicDescriptionAfterCreate, taskManager.getTaskById(lastId).getDescription());
        assertEquals(epicStatusAfterCreate, taskManager.getTaskById(lastId).getStatus());

        Subtask subtask = new Subtask("Some subtask name", "Some subtask description", lastId);
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

    @Test
    public void getTaskByTypeTest() {
        Task task = new Task("Some task name", "Some task description");
        taskManager.addTask(task);
        Epic epic = new Epic("Some epic name", "Some epic description");
        taskManager.addTask(epic);
        Subtask subtask = new Subtask("Some subtask name", "Some subtask description", epic.getId());
        taskManager.addTask(subtask);

        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        List<Epic> epics = new ArrayList<>();
        epics.add(epic);
        List<Task> subtasks = new ArrayList<>();
        subtasks.add(subtask);

        assertArrayEquals(tasks.toArray(), taskManager.getTasksByType(Task.class).toArray());
        assertArrayEquals(epics.toArray(), taskManager.getTasksByType(Epic.class).toArray());
        assertArrayEquals(subtasks.toArray(), taskManager.getTasksByType(Subtask.class).toArray());
    }

    @Test
    public void deleteTaskByTypeTest() {
        Task task = new Task("Some task name", "Some task description");
        taskManager.addTask(task);
        Epic epic = new Epic("Some epic name", "Some epic description");
        taskManager.addTask(epic);
        Subtask subtask = new Subtask("Some subtask name", "Some subtask description", epic.getId());
        taskManager.addTask(subtask);

        assertTrue(taskManager.getAllTasks().contains(task));
        assertTrue(taskManager.getAllTasks().contains(epic));
        assertTrue(taskManager.getAllTasks().contains(subtask));

        taskManager.deleteTasksByType(Task.class);
        assertFalse(taskManager.getAllTasks().contains(task));

        taskManager.deleteTasksByType(Epic.class);
        assertFalse(taskManager.getAllTasks().contains(epic));

        taskManager.deleteTasksByType(Subtask.class);
        assertFalse(taskManager.getAllTasks().contains(subtask));
    }
}

package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import util.Formats;
import util.ObjectBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    @AfterEach
    public void clearTasks() {
        taskManager.clearTasks();
    }

    @Test
    public void addTaskTest() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);

        assertTrue(taskManager.getAllTasks().contains(task1));
    }

    @Test
    public void updateTaskTest() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);
        assertTrue(taskManager.getAllTasks().contains(task1));

        String newName = "Some new name";
        task1.setName(newName);
        taskManager.updateTask(task1);

        assertEquals(newName, taskManager.getTaskById(task1.getId()).getName());
    }

    @Test
    public void deleteTaskByIdTest() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);
        assertTrue(taskManager.getAllTasks().contains(task1));
        taskManager.deleteTaskById(task1.getId());
        assertNull(taskManager.getTaskById(task1.getId()));
    }

    @Test
    public void taskStatusSuccessfullyChanged() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);
        task1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()).getStatus());
    }

    @Test
    public void epicAddedSuccessfully() {
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        assertTrue(taskManager.getAllTasks().contains(epic1));
    }

    @Test
    public void epicAndSubtasksDeletedSuccessfully() {
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        assertTrue(taskManager.getAllTasks().contains(epic1));
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic1.getId())
                .build();
        taskManager.addTask(subtask1);
        taskManager.deleteTaskById(epic1.getId());
        assertNull(taskManager.getTaskById(subtask1.getId()));
        assertNull(taskManager.getTaskById(epic1.getId()));
    }

    @Test
    public void epicStatusSuccessfullyChanged() {
        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);
        assertEquals(TaskStatus.NEW, epic1.getStatus());
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
        assertEquals(TaskStatus.NEW, epic1.getStatus());
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());
        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.NEW, epic1.getStatus());
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask2);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.DONE, epic1.getStatus());
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void subtaskAddedSuccessfully() {
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
        assertTrue(taskManager.getAllTasks().contains(subtask1));
        assertTrue(taskManager.getAllTasks().contains(subtask2));
    }

    @Test
    public void subtaskDeletedSuccessfully() {
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
        taskManager.addTask(subtask1);
        assertTrue(taskManager.getAllTasks().contains(subtask1));
        taskManager.deleteTaskById(subtask1.getId());
        assertNull(taskManager.getTaskById(subtask1.getId()));
    }

    @Test
    public void subtaskStatusSuccessfullyChanged() {
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
        taskManager.addTask(subtask1);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);
        assertEquals(TaskStatus.DONE, taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()).getStatus());
    }

    @Test
    public void subtaskShouldNotAddIfEpicIdIsIncorrect() {
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
        taskManager.addTask(subtask1);
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, subtask1.getId())
                .build();
        taskManager.addTask(subtask2);
        assertEquals(1, taskManager.getTasksByType(Subtask.class).size());
    }

    @Test
    public void getAllTasksTest() {
        List<Task> allTasks = new ArrayList<>();
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        allTasks.add(task);
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        allTasks.add(epic);
        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        taskManager.addTask(subtask);
        allTasks.add(subtask);
        assertArrayEquals(allTasks.toArray(), taskManager.getAllTasks().toArray());
    }

    @Test
    public void getSubtasksTest() {
        List<Task> allSubtasks = new ArrayList<>();
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask3 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        allSubtasks.add(subtask1);
        allSubtasks.add(subtask2);
        allSubtasks.add(subtask3);
        assertArrayEquals(allSubtasks.toArray(), taskManager.getSubtasks(epic.getId()).toArray());
    }

    @Test
    public void clearTasksTest() {
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask3 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        taskManager.clearTasks();
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    public void getTaskByIdTest() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));

        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));

        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        taskManager.addTask(subtask);
        assertNotNull(taskManager.getTaskById(InMemoryTaskManager.getLastTaskId()));
    }

    @Test
    public void getTaskByTypeTest() {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
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
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        Subtask subtask = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
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

    @Test
    public void getHistoryTest() {
        Task task = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task);
        Epic epic = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic);
        Subtask subtask1 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask2 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        Subtask subtask3 = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Some name")
                .with(Subtask::setDescription, "Some description")
                .with(Subtask::setDuration, 30L)
                .with(Subtask::setEpicId, epic.getId())
                .build();
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(epic.getId());
        taskManager.getTaskById(subtask1.getId());
        taskManager.getTaskById(subtask2.getId());
        taskManager.getTaskById(subtask3.getId());
        assertArrayEquals(taskManager.getAllTasks().toArray(), taskManager.getHistory().toArray());
    }

    @Test
    public void datesIntersectValidationTest() {
        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Подготовиться к презентации")
                .with(Task::setDescription, "Очень важно начать подготовку вовремя")
                .with(Task::setDuration, 120L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 21:30", Formats.csvDateTimeFormat))
                .build());

        taskManager.addTask(ObjectBuilder.of(Epic::new)
                .with(Task::setName, "Освоить английский на уровне B1")
                .with(Task::setDescription, "Эпик нужно завершить за 6 месяцев")
                .build());
        int idEpic1 = InMemoryTaskManager.getLastTaskId();
        taskManager.addTask(ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "New name")
                .with(Subtask::setDescription, "New description")
                .with(Subtask::setDuration, 100L)
                .with(Task::setStartTime, LocalDateTime.parse("01.02.2025 09:00", Formats.csvDateTimeFormat))
                .with(Subtask::setEpicId, idEpic1)
                .build());

        taskManager.addTask(ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Улучшить говорение")
                .with(Subtask::setDescription, "Занятия с носителем языка")
                .with(Subtask::setDuration, 500L)
                .with(Task::setStartTime, LocalDateTime.parse("10.02.2025 11:30", Formats.csvDateTimeFormat))
                .with(Subtask::setEpicId, idEpic1)
                .build());

        taskManager.addTask(ObjectBuilder.of(Epic::new)
                .with(Task::setName, "Сбросить вес к лету")
                .with(Task::setDescription, "Выбрать подходящий зал")
                .build());
        int idEpic2 = InMemoryTaskManager.getLastTaskId();

        taskManager.addTask(ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Заниматься с тренером")
                .with(Subtask::setDescription, "Ознакомиться с отзывами")
                .with(Subtask::setDuration, 1000L)
                .with(Task::setStartTime, LocalDateTime.parse("03.02.2025 07:30", Formats.csvDateTimeFormat))
                .with(Subtask::setEpicId, idEpic2)
                .build());

        //пересекается, не должно быть в getPrioritizedTasks
        Subtask st = ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Проверка пересечения")
                .with(Subtask::setDescription, "Пересекается с Task 1")
                .with(Subtask::setDuration, 10L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 23:29", Formats.csvDateTimeFormat))
                .with(Subtask::setEpicId, idEpic2)
                .build();
        taskManager.addTask(st);

        //пересекается, не должно быть в getPrioritizedTasks
        Task t = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Еще одно пересечение")
                .with(Task::setDescription, "Описание")
                .with(Task::setDuration, 120L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 20:30", Formats.csvDateTimeFormat))
                .build();
        taskManager.addTask(t);

        assertFalse(taskManager.getPrioritizedTasks().contains(st));
        assertFalse(taskManager.getPrioritizedTasks().contains(t));
    }
}

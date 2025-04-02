import controller.HttpTaskServer;
import manager.FileBackedTaskManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import task.*;
import util.Formats;
import util.ObjectBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        //testFileTaskManager();
        testHttpTaskManager();
    }

    private static void testFileTaskManager() {
        FileBackedTaskManager taskManager = (FileBackedTaskManager) Managers.getFileBacketTaskManager();
        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Подготовиться к презентации")
                .with(Task::setDescription, "Очень важно начать подготовку вовремя")
                .with(Task::setDuration, 120L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 21:30", Formats.csvDateTimeFormat))
                .build());

        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Приготовить еду на 3 дня")
                .with(Task::setDescription, "Нужно сделать до четверга")
                .with(Task::setDuration, 60L)
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
                .with(Subtask::setName, "Освоить грамматику")
                .with(Subtask::setDescription, "Нужна ежедневная практика")
                .with(Subtask::setDuration, 300L)
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

        taskManager.addTask(ObjectBuilder.of(Subtask::new)
                .with(Subtask::setName, "Проверка пересечения")
                .with(Subtask::setDescription, "Пересекается с Task 1")
                .with(Subtask::setDuration, 10L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 23:29", Formats.csvDateTimeFormat))
                .with(Subtask::setEpicId, idEpic2)
                .build());

        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Еще одно пересечение")
                .with(Task::setDescription, "Описание")
                .with(Task::setDuration, 120L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 20:30", Formats.csvDateTimeFormat))
                .build());


        System.out.println("Печать prioritizedTasks");
        System.out.println(taskManager.prioritizedTasks);

        Subtask changedSubtask = (Subtask) taskManager.getTaskById(4);
        changedSubtask.setDescription("New description1");
        changedSubtask.setName("New name1");
        changedSubtask.setStatus(TaskStatus.DONE);
        taskManager.updateTask(taskManager.getTaskById(4));
        taskManager.deleteTaskById(2);

        System.out.println(taskManager.prioritizedTasks);

        TaskManager taskManagerFromFile = FileBackedTaskManager.loadFromFile(FileBackedTaskManager.getSourcePath());
        taskManager.printAllTasks();
        System.out.println("Загрузка задач из файла работает: "
                + Arrays.equals(taskManager.getAllTasks().toArray(),
                taskManagerFromFile.getAllTasks().toArray()));

        System.out.println(taskManager.prioritizedTasks);


    }

    private static void testHttpTaskManager() throws IOException {
        TaskManager taskManager = Managers.getDefault();
        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Подготовиться к презентации")
                .with(Task::setDescription, "Очень важно начать подготовку вовремя")
                .with(Task::setDuration, 120L)
                .with(Task::setStartTime, LocalDateTime.parse("15.01.2025 21:30", Formats.csvDateTimeFormat))
                .build());

        taskManager.addTask(ObjectBuilder.of(Task::new)
                .with(Task::setName, "Приготовить еду на 3 дня")
                .with(Task::setDescription, "Нужно сделать до четверга")
                .with(Task::setDuration, 60L)
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
                .with(Subtask::setName, "Освоить грамматику")
                .with(Subtask::setDescription, "Нужна ежедневная практика")
                .with(Subtask::setDuration, 300L)
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

        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.startServer();
    }
}

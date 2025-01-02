import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        testTaskManager();
    }

    private static void testTaskManager() {
        TaskManager taskManager = new TaskManager();

        taskManager.addTask(new Task("Подготовиться к презентации",
                "Очень важно начать подготовку вовремя"));
        taskManager.addTask(new Task("Приготовить еду на 3 дня",
                "Нужно сделать до четверга"));

        taskManager.addTask(new Epic("Освоить английский на уровне B1",
                "Эпик нужно завершить за 6 месяцев"));
        int idEpic1 = TaskManager.getLastTaskId();
        taskManager.addTask(new Subtask("Освоить грамматику", "Нужна ежедневная практика", idEpic1));
        taskManager.addTask(new Subtask("Улучшить говорение", "Занятия с носителем языка", idEpic1));

        taskManager.addTask(new Epic("Сбросить вес к лету", "Выбрать подходящий зал"));
        int idEpic2 = TaskManager.getLastTaskId();
        taskManager.addTask(new Subtask("Заниматься с тренером", "Ознакомиться с отзывами",
                idEpic2));

        System.out.println("\nТестирование трекера задач");
        System.out.println("1. Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.");
        System.out.println("2. Распечатайте списки эпиков, задач и подзадач через System.out.println(..)");
        taskManager.printAllTasks();

        System.out.println("\n3. Измените статусы созданных объектов, распечатайте их. Проверьте, " +
                "что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.");
        Task task1 = taskManager.getTaskById(1);
        task1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task1);

        Task task2 = taskManager.getTaskById(2);
        task2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task2);

        Epic epic1 = (Epic) taskManager.getTaskById(3);
        epic1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(epic1);

        Subtask subtask1 = (Subtask) taskManager.getTaskById(4);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask1);

        Subtask subtask2 = (Subtask) taskManager.getTaskById(5);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateTask(subtask2);

        Subtask subtask3 = (Subtask) taskManager.getTaskById(7);
        subtask3.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(subtask3);
        taskManager.printAllTasks();

        System.out.println("\n4. И, наконец, попробуйте удалить одну из задач и один из эпиков");
        taskManager.deleteTaskById(3);
        taskManager.deleteTaskById(1);
        taskManager.deleteTaskById(7);
        taskManager.deleteTaskById(6);
        taskManager.deleteTaskById(Integer.MAX_VALUE);
        taskManager.printAllTasks();

        System.out.println("\nДополнительная проверка методов TrackerManager:");
        System.out.println("5. Получение списка всех задач");
        Map<Integer, Task> tasks = new HashMap<>();
        System.out.println(tasks);
        tasks = taskManager.getAllTasks();
        System.out.println(tasks);

        System.out.println("\n6. Получение списка всех подзадач определённого эпика");
        Map<Integer, Task> subtasks = new HashMap<>();
        System.out.println(subtasks);
        subtasks = taskManager.getSubtasks(3);
        System.out.println(subtasks);

        System.out.println("\n7. Удаление всех задач");
        taskManager.printAllTasks();
        taskManager.clearTasks();
        taskManager.printAllTasks();
    }
    
}

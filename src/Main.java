public class Main {

    public static void main(String[] args) throws CloneNotSupportedException {
        testTaskManagerHistory();
    }

    private static void testTaskManagerHistory() throws CloneNotSupportedException {
        TaskManager taskManager = Managers.getDefault();

        taskManager.addTask(new Task("Подготовиться к презентации",
                "Очень важно начать подготовку вовремя"));
        taskManager.addTask(new Task("Приготовить еду на 3 дня",
                "Нужно сделать до четверга"));

        taskManager.addTask(new Epic("Освоить английский на уровне B1",
                "Эпик нужно завершить за 6 месяцев"));
        int idEpic1 = InMemoryTaskManager.getLastTaskId();
        taskManager.addTask(new Subtask("Освоить грамматику", "Нужна ежедневная практика", idEpic1));
        taskManager.addTask(new Subtask("Улучшить говорение", "Занятия с носителем языка", idEpic1));

        taskManager.addTask(new Epic("Сбросить вес к лету", "Выбрать подходящий зал"));
        int idEpic2 = InMemoryTaskManager.getLastTaskId();
        taskManager.addTask(new Subtask("Заниматься с тренером", "Ознакомиться с отзывами",
                idEpic2));

        System.out.println("\nТестирование истории просмотра трекера задач:");
        System.out.println("\n1. В историю записываются только просмотры задач, полученных через методы get..ById()");
        System.out.println("Получение списка всех задач printAllTasks() - нет вызова get..ById()");
        taskManager.printAllTasks();
        System.out.println("Отображение истории просмотров: ");
        taskManager.getHistory().forEach(System.out::println);
        System.out.println("\nВызов метода getTaskById(1)");
        System.out.println(taskManager.getTaskById(1));
        System.out.println("Вызов метода getTaskById(2)");
        System.out.println(taskManager.getTaskById(2));
        System.out.println("Вызов метода getTaskById(4)");
        System.out.println(taskManager.getTaskById(4));
        System.out.println("Вызов метода getTaskById(7)");
        System.out.println(taskManager.getTaskById(7));
        System.out.println("Вызов метода getTaskById(2)");
        System.out.println(taskManager.getTaskById(2));
        System.out.println("\nОтображение истории просмотров: ");
        taskManager.getHistory().forEach(System.out::println);

        System.out.println("\n2. В историю просмотра записывается состояние задачи на момент просмотра");
        System.out.println("Изменение подзадачи с id = 4");
        Subtask changedSubtask = (Subtask) taskManager.getTaskById(4);
        changedSubtask.setDescription("New description");
        changedSubtask.setName("New name");
        changedSubtask.setStatus(TaskStatus.DONE);
        System.out.println("Вызов метода getTaskById(4)");
        System.out.println(taskManager.getTaskById(4));
        System.out.println("\nОтображение истории просмотров: ");
        taskManager.getHistory().forEach(System.out::println);

        System.out.println("\n3. В истории просмотра хранятся только уникальные задачи");
        System.out.println("\nВызов метода getTaskById(1)");
        System.out.println(taskManager.getTaskById(1));
        System.out.println("Вызов метода getTaskById(2)");
        System.out.println(taskManager.getTaskById(2));
        System.out.println("Вызов метода getTaskById(4)");
        System.out.println(taskManager.getTaskById(4));
        System.out.println("Вызов метода getTaskById(7)");
        System.out.println(taskManager.getTaskById(7));
        System.out.println("Вызов метода getTaskById(1)");
        System.out.println(taskManager.getTaskById(1));
        System.out.println("Вызов метода getTaskById(4)");
        System.out.println(taskManager.getTaskById(4));
        System.out.println("\nОтображение истории просмотров: ");
        taskManager.getHistory().forEach(System.out::println);
    }
}

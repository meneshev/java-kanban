package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final Path DEFAULT_PATH = Paths.get("resources/tasks.txt");
    private static Path sourcePath;
    private static final String csvHeader = "id,type,name,status,description,epicId";

    public FileBackedTaskManager() {
        sourcePath = DEFAULT_PATH;
        System.out.printf("INFO: Создан объект FileBackedTaskManager с настройкой по умолчанию. Задачи хранятся %s\n",
                sourcePath.toAbsolutePath());
    }

    public FileBackedTaskManager(Path file) {
        sourcePath = file;
        System.out.printf("INFO: Создан объект FileBackedTaskManager. Задачи хранятся %s\n",
                sourcePath.toAbsolutePath());
    }

    public FileBackedTaskManager(Map<Integer, Task> taskMap) {
        super(taskMap);
    }

    public String getCsvHeader() {
        return csvHeader;
    }

    public static Path getSourcePath() {
        return sourcePath;
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            if (!reader.ready()) {
                System.out.println("ERROR: файл пустой");
                return new FileBackedTaskManager();
            }

            String[] fieldsInFirstRow = reader.readLine().split(",");
            Map<String, Integer> fields = new HashMap<>();

            for (int i = 0; i < fieldsInFirstRow.length; i++) {
                fields.put(fieldsInFirstRow[i], i);
            }

            Map<Integer, Task> tmpTaskMap = new LinkedHashMap<>();

            while (reader.ready()) {
                String[] taskFromRow = reader.readLine().split(",");
                TaskType taskType;
                try {
                     taskType = TaskType.valueOf(taskFromRow[fields.get("type")]);
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: Ошибка при загрузке из файла: неизвестный тип задачи");
                    return new FileBackedTaskManager();
                }

                switch (taskType) {
                    case TASK:
                        tmpTaskMap.put(Integer.parseInt(taskFromRow[fields.get("id")]),
                                new Task(
                                        taskFromRow[fields.get("name")],
                                        taskFromRow[fields.get("description")],
                                        Integer.parseInt(taskFromRow[fields.get("id")]),
                                        TaskStatus.valueOf(taskFromRow[fields.get("status")])
                                    )
                                );
                        break;
                    case EPIC:
                        tmpTaskMap.put(Integer.parseInt(taskFromRow[fields.get("id")]),
                                new Epic(
                                        taskFromRow[fields.get("name")],
                                        taskFromRow[fields.get("description")],
                                        Integer.parseInt(taskFromRow[fields.get("id")]),
                                        TaskStatus.valueOf(taskFromRow[fields.get("status")])
                                    )
                                );
                        break;
                    case SUBTASK:
                        tmpTaskMap.put(Integer.parseInt(taskFromRow[fields.get("id")]),
                                new Subtask(
                                        taskFromRow[fields.get("name")],
                                        taskFromRow[fields.get("description")],
                                        Integer.parseInt(taskFromRow[fields.get("id")]),
                                        TaskStatus.valueOf(taskFromRow[fields.get("status")]),
                                        Integer.parseInt(taskFromRow[fields.get("epicId")])
                                    )
                                );
                        break;
                }
            }

            if (tmpTaskMap.isEmpty()) {
                System.out.println("ERROR: в файле не указаны задачи");
                return new FileBackedTaskManager();
            }


            System.out.printf("""
                    INFO: Создан объект FileBackedTaskManager с загруженными задачами из файла %s
                    Загружено задач из файла: %d
                    """, file.toAbsolutePath(), tmpTaskMap.size());

            return new FileBackedTaskManager(tmpTaskMap);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка во время работы с файлом");
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(sourcePath);
             BufferedReader reader = Files.newBufferedReader(sourcePath)) {
            if (taskMap.isEmpty()) {
                writer.write("");
                writer.flush();
            } else {
                if (reader.readLine() == null) {
                    writer.write(csvHeader);
                    writer.newLine();
                }

                for (Task task : taskMap.values()) {
                    writer.append(task.toCsvString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка во время работы с файлом");
        }
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }
}

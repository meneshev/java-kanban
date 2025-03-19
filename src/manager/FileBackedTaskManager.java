package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import util.Formats;
import util.ObjectBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final Path DEFAULT_PATH = Paths.get("resources/tasks.txt");
    private static Path sourcePath;
    private static final String csvHeader = "id,type,name,status,description,duration,startTime,epicId";

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
                        Task task = ObjectBuilder.of(Task::new)
                                .with(Task::setId, Integer.parseInt(taskFromRow[fields.get("id")]))
                                .with(Task::setName, taskFromRow[fields.get("name")])
                                .with(Task::setStatus, TaskStatus.valueOf(taskFromRow[fields.get("status")]))
                                .with(Task::setDescription, taskFromRow[fields.get("description")])
                                .with(Task::setDuration, Long.parseLong(taskFromRow[fields.get("duration")]))
                                .build();

                        if (taskFromRow.length > 6) {
                            task.setStartTime(LocalDateTime.parse(taskFromRow[fields.get("startTime")],
                                    Formats.csvDateTimeFormat));
                        }

                        tmpTaskMap.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = ObjectBuilder.of(Epic::new)
                                .with(Epic::setId, Integer.parseInt(taskFromRow[fields.get("id")]))
                                .with(Epic::setName, taskFromRow[fields.get("name")])
                                .with(Epic::setStatusForce, TaskStatus.valueOf(taskFromRow[fields.get("status")]))
                                .with(Epic::setDescription, taskFromRow[fields.get("description")])
                                .build();

                        tmpTaskMap.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        Subtask subtask = ObjectBuilder.of(Subtask::new)
                                .with(Subtask::setId, Integer.parseInt(taskFromRow[fields.get("id")]))
                                .with(Subtask::setName, taskFromRow[fields.get("name")])
                                .with(Subtask::setStatus, TaskStatus.valueOf(taskFromRow[fields.get("status")]))
                                .with(Subtask::setDescription, taskFromRow[fields.get("description")])
                                .with(Subtask::setDuration, Long.parseLong(taskFromRow[fields.get("duration")]))
                                .with(Subtask::setEpicId, Integer.parseInt(taskFromRow[fields.get("epicId")]))
                                .build();

                        if (!taskFromRow[fields.get("startTime")].isBlank()) {
                            subtask.setStartTime(LocalDateTime.parse(taskFromRow[fields.get("startTime")],
                                    Formats.csvDateTimeFormat));
                        }

                        tmpTaskMap.put(subtask.getId(),subtask);
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

//                for (Task task : taskMap.values()) {
//                    writer.append(task.toCsvString());
//                    writer.newLine();
//                }
                taskMap.values().forEach(task -> {
                    try {
                        writer.append(task.toCsvString());
                        writer.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
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

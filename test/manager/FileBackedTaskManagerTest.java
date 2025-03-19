package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import util.ObjectBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeEach
    public void prepareFileBackedTaskManager() {
        try {
            Path tmpPath = Files.createTempFile( "temp", ".txt");
            taskManager = new FileBackedTaskManager(tmpPath);
        } catch (IOException e) {
            System.out.println("ERROR: проблема при создании временного файла");
        }
    }

    @AfterEach
    void setUp() {
        taskManager.clearTasks();
    }


    @Test
    void loadFromFile() throws IOException {
        Path file = Path.of("test/resources/fileForLoad.txt");

        assertDoesNotThrow(() -> {
            taskManager = FileBackedTaskManager.loadFromFile(file);
        });

        List<String> tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .toList();

        List<String> tasksInCsvFile;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .toList();
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }

    @Test
    public void clearTasks() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);

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
                .with(Subtask::setEpicId, epic1.getId())
                .build();
        taskManager.addTask(subtask2);

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            assertEquals(task1.toCsvString(), reader.readLine());
            taskManager.clearTasks();
            StringBuilder fileAfterClear = new StringBuilder();
            List<String> fileLines =  Files.readAllLines(FileBackedTaskManager.getSourcePath());
            fileLines.forEach(fileAfterClear::append);
            assertEquals("", fileAfterClear.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void addTask() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);

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
                .with(Subtask::setEpicId, epic1.getId())
                .build();
        taskManager.addTask(subtask2);

        List<String> tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .toList();

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }

    @Test
    void updateTask() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);


        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);

        List<String> tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());

        task1.setDescription("New description");
        epic1.setName("New name");
        taskManager.updateTask(task1);
        taskManager.updateTask(epic1);

        tasksInCsvRAM.clear();

        tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .toList();

        tasksInCsvFile.clear();

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }


        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }

    @Test
    void deleteTaskById() {
        Task task1 = ObjectBuilder.of(Task::new)
                .with(Task::setName, "Some name")
                .with(Task::setDescription, "Some description")
                .with(Task::setDuration, 30L)
                .build();
        taskManager.addTask(task1);

        Epic epic1 = ObjectBuilder.of(Epic::new)
                .with(Epic::setName, "Some name")
                .with(Epic::setDescription, "Some description")
                .build();
        taskManager.addTask(epic1);

        List<String> tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());

        taskManager.deleteTaskById(1);

        tasksInCsvRAM.clear();
        tasksInCsvRAM = taskManager.getAllTasks().stream()
                .map(Task::toCsvString)
                .toList();

        tasksInCsvFile.clear();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            tasksInCsvFile = reader.lines()
                    .skip(1)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }
}
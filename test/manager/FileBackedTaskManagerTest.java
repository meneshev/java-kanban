package manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static FileBackedTaskManager manager;

    @BeforeAll
    static void prepareFileBackedTaskManager() {
        try {
            Path tmpPath = Files.createTempFile( "temp", ".txt");
            manager = new FileBackedTaskManager(tmpPath);
        } catch (IOException e) {
            System.out.println("ERROR: проблема при создании временного файла");
        }
    }

    @BeforeEach
    void setUp() {
        manager.clearTasks();
    }

    @Test
    void loadFromFile() throws IOException {
        final Path file = Path.of("test/resources/fileForLoad.txt");
        manager = FileBackedTaskManager.loadFromFile(file);

        List<String> tasksInCsvRAM = new ArrayList<>();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); // заголовок не нужен
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }

    @Test
    void clearTasks() {
        Task task1 = new Task("Some name", "Some description");
        manager.addTask(task1);

        Epic epic1 = new Epic("Some name", "Some description");
        manager.addTask(epic1);

        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        manager.addTask(subtask1);

        Subtask subtask2 = new Subtask("Some name", "Some description", epic1.getId());
        manager.addTask(subtask2);

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            assertEquals(task1.toCsvString(), reader.readLine());
            manager.clearTasks();
            StringBuilder fileAfterClear = new StringBuilder();
            List<String> fileLines =  Files.readAllLines(FileBackedTaskManager.getSourcePath());
            for (String s : fileLines) fileAfterClear.append(s);
            assertEquals("", fileAfterClear.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addTask() {
        Task task1 = new Task("Some name", "Some description");
        manager.addTask(task1);

        Epic epic1 = new Epic("Some name", "Some description");
        manager.addTask(epic1);

        Subtask subtask1 = new Subtask("Some name", "Some description", epic1.getId());
        manager.addTask(subtask1);

        Subtask subtask2 = new Subtask("Some name", "Some description", epic1.getId());
        manager.addTask(subtask2);

        List<String> tasksInCsvRAM = new ArrayList<>();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
    }

    @Test
    void updateTask() {
        Task task1 = new Task("Some name", "Some description");
        manager.addTask(task1);

        Epic epic1 = new Epic("Some name", "Some description");
        manager.addTask(epic1);

        List<String> tasksInCsvRAM = new ArrayList<>();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
            assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        task1.setDescription("New description");
        epic1.setName("New name");
        manager.updateTask(task1);
        manager.updateTask(epic1);

        tasksInCsvRAM.clear();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            tasksInCsvFile.clear();
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
            assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteTaskById() {
        Task task1 = new Task("Some name", "Some description");
        manager.addTask(task1);

        Epic epic1 = new Epic("Some name", "Some description");
        manager.addTask(epic1);

        List<String> tasksInCsvRAM = new ArrayList<>();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        List<String> tasksInCsvFile = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
            assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        manager.deleteTaskById(1);

        tasksInCsvRAM.clear();
        for (Task t : manager.getAllTasks().values()) {
            tasksInCsvRAM.add(t.toCsvString());
        }

        try (BufferedReader reader = Files.newBufferedReader(FileBackedTaskManager.getSourcePath())) {
            reader.readLine(); // заголовок не нужен
            tasksInCsvFile.clear();
            while (reader.ready()) {
                tasksInCsvFile.add(reader.readLine());
            }
            assertArrayEquals(tasksInCsvRAM.toArray(), tasksInCsvFile.toArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
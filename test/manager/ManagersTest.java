package manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class ManagersTest {
    @Test
    public void defaultTaskManagerIsNotNull() {
        assertNotNull(Managers.getDefault());
    }

    @Test
    public void defaultHistoryManagerIsNotNull() {
        assertNotNull(Managers.getDefaultHistory());
    }
}

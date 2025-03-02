package task;

public class Epic extends Task {

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, Integer id, TaskStatus status) {
        super(name, description, id, status);
    }

    @Override
    public void setStatus(TaskStatus status) {
        System.out.println("WARN: Для эпиков запрещена ручная смена статуса");
    }

    public void setStatus(TaskStatus status, boolean force) {
        if (force) {
            super.setStatus(status);
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + super.getName() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", id=" + super.getId() +
                ", status=" + super.getStatus() +
                '}';
    }
}

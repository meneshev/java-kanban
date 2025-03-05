package task;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Integer id, TaskStatus status, Integer epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + super.getName() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", id=" + super.getId() +
                ", status=" + super.getStatus() +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toCsvString() {
        return new StringBuilder(super.toCsvString())
                .append(",")
                .append(getEpicId())
                .toString();
    }
}

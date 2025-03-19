package task;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask() {
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
                ", duration=" + super.getDuration().toMinutes() +
                ", startTime=" + super.getStartTime().orElse(null) +
                ", endTime=" + super.getEndTime().orElse(null) +
                '}';
    }

    @Override
    public String toCsvString() {
        return super.toCsvString() +
                getEpicId();
    }
}

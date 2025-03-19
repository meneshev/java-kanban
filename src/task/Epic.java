package task;

import java.time.LocalDateTime;
import java.util.Optional;

public class Epic extends Task {

    private LocalDateTime endTime;

    public Epic() {
    }

    @Override
    public void setStatus(TaskStatus status) {
        System.out.println("WARN: Для эпиков запрещена ручная смена статуса");
    }

    public void setStatusForce(TaskStatus status) {
        super.setStatus(status);

    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + super.getName() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", id=" + super.getId() +
                ", status=" + super.getStatus() +
                ", duration=" + super.getDuration().toMinutes() +
                ", startTime=" + super.getStartTime().orElse(null) +
                ", endTime=" + getEndTime().orElse(null) +
                '}';
    }

    public String toCsvString() {
        StringBuilder csvString = new StringBuilder(super.getId().toString()).append(",")
                .append(this.getClass().getSimpleName().toUpperCase()).append(",")
                .append(super.getName()).append(",")
                .append(super.getStatus().name()).append(",")
                .append(super.getDescription());
        return csvString.append(",,,").toString();
    }
}

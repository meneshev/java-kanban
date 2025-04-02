package task;

import util.Formats;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task implements Cloneable {
    private String name;
    private String description;
    private Integer id;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;

    public Task() {
        this.status = TaskStatus.NEW;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = Duration.ofMinutes(duration);
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Optional<LocalDateTime> getEndTime() {
        return getStartTime().isPresent() && duration != null ?
                Optional.of(startTime.plusMinutes(duration.toMinutes())) : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        return (Task) super.clone();
    }

    @Override
    public String toString() {
        Long duration = this.duration != null ? this.duration.toMinutes() : null;
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime().orElse(null) +
                '}';
    }

    public String toCsvString() {
        StringBuilder csvString = new StringBuilder(id.toString()).append(",")
                .append(this.getClass().getSimpleName().toUpperCase()).append(",")
                .append(name).append(",")
                .append(status.name()).append(",")
                .append(description).append(",")
                .append(duration.toMinutes()).append(",");

        if (this.startTime != null) {
            csvString.append(Optional.of(startTime).get().format(Formats.csvDateTimeFormat));
        }
        return csvString.append(",").toString();
    }
}

package model;

import java.time.LocalDateTime;

public class LogEntity {
    private int id;
    private LocalDateTime launchTime;

    public LogEntity() {}

    public LogEntity(int id, LocalDateTime launchTime) {
        this.id = id;
        this.launchTime = launchTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(LocalDateTime launchTime) {
        this.launchTime = launchTime;
    }

    @Override
    public String toString() {
        return "LogEntity{id=" + id + ", launchTime=" + launchTime + "}";
    }
}

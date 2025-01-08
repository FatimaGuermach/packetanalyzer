package model;

public class ThreatEntity {
    private int id;
    private String threatLevel; // ENUM('Low', 'Medium', 'High')

    public ThreatEntity() {}

    public ThreatEntity(int id, String threatLevel) {
        this.id = id;
        this.threatLevel = threatLevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }

    @Override
    public String toString() {
        return "ThreatEntity{id=" + id + ", threatLevel='" + threatLevel + "'}";
    }
}

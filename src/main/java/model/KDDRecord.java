package model;

public class KDDRecord {
    private final String feature1;
    private final String feature2;

    public KDDRecord(String feature1, String feature2) {
        this.feature1 = feature1;
        this.feature2 = feature2;
    }

    public String getFeature1() {
        return feature1;
    }

    public String getFeature2() {
        return feature2;
    }
}

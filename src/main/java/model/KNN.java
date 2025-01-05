package model;

import java.util.*;

public class KNN {
    private List<double[]> dataPoints;
    private List<String> labels;

    public KNN(List<double[]> dataPoints, List<String> labels) {
        this.dataPoints = dataPoints;
        this.labels = labels;
    }

    public String classify(double[] inputPoint, int k) {
        PriorityQueue<double[]> distances = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

        for (int i = 0; i < dataPoints.size(); i++) {
            double distance = euclideanDistance(inputPoint, dataPoints.get(i));
            distances.add(new double[] {distance, i});
        }

        Map<String, Integer> labelCount = new HashMap<>();
        for (int i = 0; i < k; i++) {
            int index = (int) distances.poll()[1];
            String label = labels.get(index);
            labelCount.put(label, labelCount.getOrDefault(label, 0) + 1);
        }

        return Collections.max(labelCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}

package model;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkStatistics {
    private AtomicInteger activeConnections = new AtomicInteger(0);

    public void incrementConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementConnections() {
        activeConnections.decrementAndGet();
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }
}

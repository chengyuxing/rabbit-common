package com.github.chengyuxing.common;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Simple Watch dog.
 */
public class WatchDog {
    private final ScheduledExecutorService schedule;
    private final Map<String, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();

    /**
     * Constructed WatchDog with max idle.
     *
     * @param maxIdle max idle
     */
    public WatchDog(int maxIdle) {
        this.schedule = Executors.newScheduledThreadPool(maxIdle);
    }

    /**
     * Constructed WatchDog with default idle 8.
     */
    public WatchDog() {
        this.schedule = Executors.newScheduledThreadPool(8);
    }

    /**
     * Add a listener.
     *
     * @param name     name
     * @param runnable runnable
     * @param period   period
     * @param unit     unit
     * @return true if added or false
     */
    public boolean addListener(String name, Runnable runnable, int period, TimeUnit unit) {
        if (futureMap.containsKey(name)) {
            return false;
        }
        long seconds = unit.toSeconds(period);
        futureMap.put(name, schedule.scheduleAtFixedRate(runnable, 1, seconds, TimeUnit.SECONDS));
        return true;
    }

    /**
     * Add a listener with default 1 period.
     *
     * @param name     name
     * @param runnable runnable
     * @return true if added or false
     */
    public boolean addListener(String name, Runnable runnable) {
        return addListener(name, runnable, 1, TimeUnit.SECONDS);
    }

    /**
     * Remove listener.
     *
     * @param name name
     * @return true if exists and removed or false
     */
    public boolean removeListener(String name) {
        if (futureMap.containsKey(name)) {
            ScheduledFuture<?> scheduledFuture = futureMap.get(name);
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                futureMap.remove(name);
            }
            return true;
        }
        return false;
    }

    /**
     * Shutdown and cleanup.
     */
    public void shutdown() {
        schedule.shutdown();
        futureMap.clear();
    }

    /**
     * Shutdown and cleanup.
     *
     * @param timeForWaiting await terminal time for waiting
     * @param unit           unit
     * @throws InterruptedException ex
     */
    public void shutdown(long timeForWaiting, TimeUnit unit) throws InterruptedException {
        //noinspection ResultOfMethodCallIgnored
        schedule.awaitTermination(timeForWaiting, unit);
        schedule.shutdown();
        futureMap.clear();
    }
}

package com.github.chengyuxing.common;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 看门狗
 */
public class WatchDog {
    private final ScheduledExecutorService schedule;
    private final Map<String, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param maxIdle 线程池最大空闲
     */
    public WatchDog(int maxIdle) {
        this.schedule = Executors.newScheduledThreadPool(maxIdle);
    }

    /**
     * 构造函数（线程池最大空闲默认8）
     */
    public WatchDog() {
        this.schedule = Executors.newScheduledThreadPool(8);
    }

    /**
     * 添加一个监听
     *
     * @param name     名称
     * @param runnable 执行方法
     * @param period   执行周期
     * @param unit     单位
     * @return 是否添加成功
     */
    public boolean addListener(String name, Runnable runnable, int period, TimeUnit unit) {
        if (futureMap.containsKey(name)) {
            return false;
        }
        futureMap.put(name, schedule.scheduleAtFixedRate(runnable, 1, period, unit));
        return true;
    }

    /**
     * 添加一个监听
     *
     * @param name     名称
     * @param runnable 执行方法
     * @return 是否添加成功
     */
    public boolean addListener(String name, Runnable runnable) {
        return addListener(name, runnable, 1, TimeUnit.SECONDS);
    }

    /**
     * 移除监听
     *
     * @param name 名称
     * @return 如果监听不存在返回false，如果移除成功返回true
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
     * 关闭看门狗
     */
    public void shutdown() {
        schedule.shutdown();
        futureMap.clear();
    }

    /**
     * 关闭看门狗
     *
     * @param timeForWaiting 等待监听完成关闭超时时间
     * @param unit           单位
     * @throws InterruptedException 如果线程中止
     */
    public void shutdown(long timeForWaiting, TimeUnit unit) throws InterruptedException {
        //noinspection ResultOfMethodCallIgnored
        schedule.awaitTermination(timeForWaiting, unit);
        schedule.shutdown();
        futureMap.clear();
    }
}

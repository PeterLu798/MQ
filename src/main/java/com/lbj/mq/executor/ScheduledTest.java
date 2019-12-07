package com.lbj.mq.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduledTest {
    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(10);

    public static void main(String[] args) {
    }
}

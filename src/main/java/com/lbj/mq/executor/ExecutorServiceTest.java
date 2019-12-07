package com.lbj.mq.executor;

import java.util.concurrent.*;

public class ExecutorServiceTest {
    static ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 100, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        Executors.newFixedThreadPool(100);
        Executors.newSingleThreadExecutor();
        Executors.newCachedThreadPool();

        executorService.prestartAllCoreThreads();
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<String>(1000);
        LinkedBlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<>();
        SynchronousQueue<String> synchronousQueue = new SynchronousQueue<>();
        PriorityBlockingQueue priorityBlockingQueue = new PriorityBlockingQueue();

    }
}

package com.lbj.mq.executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1000);

    /**
     * 不需要返回值
     */
    public void test() {
        for (int i = 0; i < 10000; i++) {
            CompletableFuture.runAsync(() -> {
                // TODO sth...
            }, executorService);
        }
    }

    /**
     * 如果需要返回值，可配合使用CountDownLatch
     *
     * @return
     */
    public boolean test1() {
        CountDownLatch countDownLatch = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            CompletableFuture.runAsync(() -> {
                //TODO sth...
            }, executorService).whenComplete((result, ex) -> countDownLatch.countDown());
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}

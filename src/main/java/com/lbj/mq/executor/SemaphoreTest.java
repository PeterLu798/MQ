package com.lbj.mq.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreTest {
    private static Semaphore semaphore = new Semaphore(10);
    private static ExecutorService executorService = Executors.newFixedThreadPool(30);

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //获取许可证
                        semaphore.acquire();
                        System.out.println("update data");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        //释放许可证
                        semaphore.release();
                    }
                }
            });
        }
        executorService.shutdown();
    }
}

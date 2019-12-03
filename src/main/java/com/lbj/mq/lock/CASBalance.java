package com.lbj.mq.lock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lubaijiang
 * 使用CAS实现转账
 */
public class CASBalance {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1000);
    private static AtomicInteger balance = new AtomicInteger(0);

    public static void transfer(int amount){
        balance.addAndGet(amount);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(10000);
        for(int i=0; i<10000; i++){
            CompletableFuture.runAsync(() -> {
                transfer(1);
            }, executorService).whenComplete((result, ex) -> countDownLatch.countDown());
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("账户余额：" + balance);
        System.out.println("总耗时：" + (System.currentTimeMillis() - startTime) + " 毫秒");
        System.exit(0);
    }
}

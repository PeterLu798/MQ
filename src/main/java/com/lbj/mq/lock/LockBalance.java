package com.lbj.mq.lock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lubaijiang
 * 使用锁实现账户转账
 */
public class LockBalance {

    private static ExecutorService executorService = Executors.newFixedThreadPool(1000);
    private static Lock lock = new ReentrantLock();
    private static int balance = 0;

    public static void transfer(int amount){
        try {
            lock.lock();
            balance = balance + amount;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        CountDownLatch count = new CountDownLatch(10000);
        for(int i=0; i<10000; i++){
            CompletableFuture.runAsync(() -> transfer(1), executorService)
            .whenComplete((result, ex) -> count.countDown());
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("账户余额：" + balance);
        System.out.println("总耗时：" + (System.currentTimeMillis() - startTime) + " 毫秒");
        System.exit(0);
    }
}

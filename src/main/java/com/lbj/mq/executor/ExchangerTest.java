package com.lbj.mq.executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangerTest {
    private static Exchanger<Integer> exchanger = new Exchanger<>();
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        CompletableFuture.runAsync(() -> {
            Integer A = 1;
            try {
                Integer C = exchanger.exchange(A);
                System.out.println("A拿到的数据是：" + C);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executorService);

        CompletableFuture.runAsync(() -> {
            Integer B = 2;
            try {
                Integer C = exchanger.exchange(B);
                System.out.println("B拿到的数据是：" + C);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executorService);
    }
}

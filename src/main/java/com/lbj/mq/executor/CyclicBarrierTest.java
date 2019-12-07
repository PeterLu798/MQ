package com.lbj.mq.executor;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {
    private static CyclicBarrier c = new CyclicBarrier(2);

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    c.await();
                    System.out.println(1);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            c.await();
            System.out.println(2);
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public static class CyclicBarrierTest2 {
        private static CyclicBarrier c = new CyclicBarrier(2, new A());

        public static void main(String[] args) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        c.await();
                        System.out.println(1);
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            try {
                c.await();
                System.out.println(2);
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        static class A implements Runnable {
            @Override
            public void run() {
                System.out.println(3);
            }
        }
    }
}

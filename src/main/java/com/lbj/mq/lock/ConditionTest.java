package com.lbj.mq.lock;

import java.util.concurrent.locks.*;

public class ConditionTest {
    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readWriteLock.readLock();
        condition.await();
    }
}

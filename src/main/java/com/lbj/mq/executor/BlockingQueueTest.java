package com.lbj.mq.executor;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;

public class BlockingQueueTest {
    public static void main(String[] args) {
        ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(16);
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
        SynchronousQueue synchronousQueue = new SynchronousQueue();
        DelayQueue delayQueue = new DelayQueue();
    }
}

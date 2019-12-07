package com.lbj.mq.executor;

import java.util.concurrent.*;

public class FutureTaskTest {
    private static ThirdInterface thirdInterface = new ThirdInterface();
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        Callable call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return thirdInterface.excuteCrud();
            }
        };
        /**
         * 使用Future Task的第一种方式：使用线程池
         */
        Future<String> future = executorService.submit(call);
        try {
            //设置超时时间为1秒
            String result = future.get(1, TimeUnit.SECONDS);
            System.out.println("ok".equals(result));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        /**
         * 使用Future Task的第二种方式，直接使用FutureTask
         */
        FutureTask<String> futureTask = new FutureTask<>(call);
        futureTask.run();
        try {
            String result = futureTask.get(1, TimeUnit.SECONDS);
            System.out.println(result);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}

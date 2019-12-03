package com.lbj.mq.junit;

public class Test {
    private int a = 123;
    private static int b = 456;
    public Test(){
        System.out.println("执行构造函数");
    }

    static {
        System.out.println("执行static");
    }

    {
        System.out.println("实例初始化块");
    }
}

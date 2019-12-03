package com.lbj.mq.junit;

import com.lbj.mq.cache.LruCacheImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MyTest {

    @Mock
    LruCacheImpl lruCache;

    @Before
    public void init() {
        System.out.println("初始化");
    }

    @After
    public void terminate() {
        System.out.println("结束销毁");
    }

    @org.junit.Test
    public void test() {
        try {
            System.out.println("单元测试");
            lruCache.testMock();
            assertThat("j").isEqualTo("j");
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e).hasMessageContaining("ddd");
        }
        new Integer(1);
        Integer a = Integer.valueOf(128);
    }

    public static void main(String[] args) {
//        com.lbj.mq.test.Test test = new com.lbj.mq.test.Test();
//        com.lbj.mq.test.Test test1 = new com.lbj.mq.test.Test();
        com.lbj.mq.junit.Test test2 = new com.lbj.mq.junit.Test();
//        com.lbj.mq.test.Test[] array = new com.lbj.mq.test.Test[10];
    }
}

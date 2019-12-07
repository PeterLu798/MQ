# MQ

* [1. 为什么需要消息队列](#1)
* [2. 该如何选择消息队列](#2)
* [3. 主题和队列有什么区别](#3)
* [4. 利用事务消息实现分布式事务](#4)
* [5. 如何确保消息不会丢失](#5)
* [6. 如何防止重复消费](#6)
* [7. 消息积压如何解决](#7)
* [8. 如何使用异步设计提升系统性能](#8)
* [9. Java中的线程池和并发工具类](#9)
10. 消息队列中的高性能序列化和反序列化
11. 消息队列中的高性能传输协议
12. 内存管理：如何避免内存溢出和频繁GC
13. kafka如何实现高性能I/O
14. 如何使用缓存来减少磁盘I/O
15. Java中的锁以及如何选择适用于自己的锁
16. 数据压缩：时间换空间的游戏
17. 源码分析：RocketMQ是如何发消息的
18. 源码分析：RocketMQ是如何消费消息的
19. 源码分析：Kafka是如何消费消息的
20. RocketMQ和Kafka消息复制实现的差异点在哪
21. RocketMQ客户端如何在集群中找到正确的节点
22. Kafka的服务协调者：Zookeeper
23. RocketMQ与Kafka如何实现事务
24. 物联网海量在线设备通信协议：MQTT

<h3 id="1">1. 为什么需要消息队列</h3>
哪些问题适合消息队列来解决

* 异步处理  
* 流量控制
* 服务解耦

其中流量控制最常见的有两种方式：<br>
一、在网关和后端服务之间加消息队列 <br>
App -> 网关 -> 消息队列 -> 秒杀服务 <br>
二、使用令牌桶 <br>
App -> 网关 -> 令牌队列 -> 秒杀服务 <br>
令牌桶 -> 生产 -> 令牌队列 <br>
网关在收到请求时，先去令牌队列中获取一个令牌，获取成功则继续调用秒杀服务，否则返回失败。<br>

<h3 id="2">2. 该如何选择消息队列</h3>
2.1 选择消息队列产品的基本标准

* 开源
* 近年来较流行的，有一定社区活跃度的
* 流行的产品与周边生态系统有一个较好的兼容 
* 产品本身一些机制：消息可靠传递，保证不丢消息、支持水平扩展（有集群模式）、高性能

2.2 比较几个比较流行的MQ产品

* RabbitMQ
<br>优点：轻量、迅捷、开箱即用。拥有灵活的路由配置（Exchange）
<br>缺点：性能和吞吐量一般为几万到十几万条每秒，Erlang语言开发，二次开发较难
* RocketMQ
<br>优点：性能好，每秒大约能处理几十万条消息、稳定可靠，特点响应快，中文社区活跃
<br>缺点：与国际一些生态产品兼容性略逊一筹
* Kafka
<br>优点：拥有强大的性能和吞吐量，流式计算大数据生态兼容性好
<br>缺点：由于“攒一波再处理”的特性，数据量小时反而延迟高

<h3 id="3">3. 主题和队列有什么区别</h3>
3.1 消息队列的发展演进

* 队列模型
<br>队列模型中的概念：
<br>生产者：生产消息的一方
<br>消费者：消费消息的一方
<br>队列：服务端存放消息的容器
<br>消息队列最初就是一个先进先出的线性表，这个线性表只允许在后端进行插入操作，在前端进行删除操作，并且在出队入队过程中保证严格有序。由于消息只能被删除一次，因此只能被消费一次，无法满足一份消息分发给多个消费者的场景，比如对于一份订单数据，支付系统、风控系统、分析系统都需要一份，这时就演化出了发布-订阅模型
* 发布订阅模型
<br>发布订阅模型中的概念：
<br>消息发布者：生产消息的一方
<br>订阅者：消费消息的一方
<br>主题：服务端存放消息的容器
<br>可以看出这和队列模型中的概念是相符的，发布者即生产者，订阅者即消费者，主题即队列。它们唯一的区别在于一份消息如何被消费多次的问题。下面我们就具体的说一说。

3.2 现有几个主流MQ的模型

* RabbitMQ
<br>RabbitMQ是少数依然使用队列模型的产品之一。那么它怎么解决多消费问题呢？
<br>首先RabbitMQ在生产者和队列之间有一个Exchange的路由交换机，生产者直接将消息发送到Exchange而不是队列中，而Exchange通过配置多个队列，发送多份完整的消息到队列，从而实现多消费的问题。虽然这种做法比较费劲，但好在RabbitMQ采用的是内存存储数据，因此性能也还可以。
* RocketMQ
<br>RocketMQ使用的模型是发布-订阅模型
<br>先来看看RocketMQ中几个重要的概念：
<br>&emsp;发布者、订阅者、主题：这些和模型中的概念一模一样
<br>&emsp;队列：此队列非“队列模型”中的“队列”。它的作用就是为了横向扩展。现在我们知道发布者将消息生产到服务端，也就是Broker，在Broker里用主题来存储消息，而一个主题包含多个队列。可以理解为发布者将消息发送到了这个主题的每个队列里，每个队列分管来自生产者的若干消息。其实将队列理解为分区更为贴切。一个主题被分成了若干个区域，每个区域存储一部分消息。而实际上Kafka就将队列叫做分区。
<br>有了队列（分区）就可以通过横向扩展增加队列和消费者的方式提高系统性能了，这也是为什么RocketMQ的数量级要远大于RabbitMQ的原因。
<br>但是你发现问题了没？消息发送给多个队列，那么如何保证消息的有序性？
<br>其实RocketMQ在主题层面不保证消息的有序性，它只在队列层面保证严格的队列顺序。那么要想发送有序消息就要考虑将消息发送到同一个队列中去。具体的做法可以参考apache rocketmq官网的例子https://rocketmq.apache.org/docs/order-example/
<br>在实际应用场景中，很多消息没必要保证有序性，这时我们就可以使用队列（分区）来做水平扩展了。
<br>那么每个队列如何保证消息的有序性呢？这得从消费确认机制说起。消息队列为了保证消息不丢以及消息的有序性，增加了请求-确认机制，在生产一端，生产者将消息发送给Broker后，只有接受到Broker返回的“成功确认“才能接着发送下一条消息，否则会重复发送该条消息，在消费一端，队列将消息发送给消费者后，也只有收到消费者的成功确认才能发下一条，否则也会重复发送。这个机制就很好的保证了消息传递的可靠性和有序性。
<br>&emsp;消费组：RocketMQ中订阅者是通过消费组来体现的。每个消费组可以得到主题的一份完整的消息，不同消费组之间不受任何影响。每个消费组中包含多个消费者，同一组内的消费者是竞争的关系，比如一条消息被消费者1消费了，那么该组内其他消费者就不能再消费这条消息了。
<br>&emsp;消费位置：一个队列可能对应多个消费组，那么如何标识队列中的某条消息有没有被消费过呢？为了记录这个信息，RocketMQ为每个消费组对应每个队列维护了一个位置变量，称为消费位置。有了消费位置，服务端就可以通过计算那些消息被全部消费过了，然后可以从队列中删除掉了。
<br>RocketMQ的结构图如下<br>
![](https://github.com/PeterLu798/MQ.png)
* Kafka
<br>Kafka的模型以及概念和RocketMQ的一模一样，只是把队列叫做分区，上面也已经说了。

<h3 id="4">4. 利用事务消息实现分布式事务</h3>
&emsp;本节主要使用RocketMQ的事务消息来实现分布式事务。
<br>&emsp;RocketMQ主要使用“半消息”来实现事务消息。“半消息”不是半个消息，消息本身是完整的，只是在发出以后消费者不能立即去消费它，此时该消息对于消费者是不可见的。当本地事务执行完成之后，会返回成功或失败结果，只有返回成功这个半消息才会发送给消费者。
<br>&emsp;我们使用一个具体场景来实践下事务消息：创建完订单之后清除购物车。这个过程中，我们假设在订单产生的瞬间发送事务消息给购物车系统，然后在本地事务的创建订单，然后根据创建结果决定是不是要清除购物车。整个过程的时序图如下：<br>
![](https://github.com/PeterLu798/MQ.png)
<br>根据时序图，订单系统在接收到消息之后，发送给MQ服务端一个半消息，然后本地创建订单，如果返回成功则MQ服务端会将半消息发送给购物车系统，然后就可以执行清除购物车逻辑了，如果返回失败则MQ服务端会将半消息删除掉，这样就不会发送给购物车系统，购物车里的商品也不会清除。但是如果创建订单成功/失败了，但是在返回的路上发生了网络故障丢了，也就是MQ服务端没拿到结果，这时RocketMQ的另一个机制就登台了：定时查询事务状态接口，根据结果继续执行投递消息或者删除消息的逻辑。为了支持这个机制，我们需要在订单系统写一个订单是否创建成功的查询接口。
<br>&emsp;事务消息的具体实现可以参考com.lbj.mq.rocketmq.TransactionListenerImpl和com.lbj.mq.rocketmq.TransactionProducer

<h3 id="5">5. 如何确保消息不会丢失</h3>
5.1 如何追踪消息
<br>&emsp;对于RocketMQ、Kafka这种发布-订阅模型的消息队列，主题不能保证消息的有序性，因此通过给消息追加序列号追踪消息的做法一定要指定是哪个分区(RocketMQ的队列)。如果Producer是多实例，则也要指定是哪个Producer，在此基础上，最好每个分区(队列)对应一个消费者，这样每个Producer+每个分区发出来的消息序列号就是递增的，每个消费者收到的序列号也是递增的，如果断了就说明丢消息了。
<br>5.2 现有消息队列是如何保证消息不丢的
<br>&emsp;我们已经知道消息队列通过请求-确认机制(ack机制)来保证消息的可靠传递的。ack机制的原理已经在第3节说过了。既然消息队列已经提供了ack机制，那怎么还会丢消息呢？其实大多情况下问题都出在了我们没用好这个机制。
<br>&emsp;消息的传递可以大致分为三个阶段：生产阶段(包括发送给Broker)、存储阶段、消费阶段(包括从Broker拉取消息)。这三个阶段中需要我们编码实现的是第一、三阶段。
<br>&emsp;在第一阶段实现生产者时，将消息发送给Broker一定要判断返回结果，或者捕获异常，如果返回结果失败或发生异常，则要重复发送，这样就能保证消息在生产阶段不丢。而且在这里一定要注意异步发送，如果我们采用异步发送，则一定要判断回调函数，这也是被很多人忽略的一点。可以参考com.lbj.mq.rocketmq.AsyncProducer中的异步发送。
<br>&emsp;在第三阶段实现消费者时，一定要确保你的业务真实完成后再返回成功，也要注意异常处理。
<br>&emsp;至于第二阶段存储阶段，我们可以根据实际需要进行必要配置来实现可靠存储，比如在RocketMQ中，可以将刷盘方式 flushDiskType 配置为 SYNC_FLUSH 同步刷盘。如果是 Broker 是由多个节点组成的集群，需要将Broker集群配置成：至少将消息发送到 2 个以上的节点，再给客户端回复发送确认响应。这样当某个Broker宕机时，其他的Broker可以替代宕机的Broker，也不会发生消息丢失。

<h3 id="6">6. 如何防止重复消费</h3>
一句话：实现业务幂等性。
<br>&emsp;如果是插入操作，最好有唯一键约束
<br>&emsp;如果是更新操作，最好有状态限制
<br>那么消息队列产品本身能不能限制重复消费呢，答案是不能。虽然MQTT协议给出了三种传递消息时的质量标准，其中有 Exactly once，但是至今还没有哪个消息队列能实现之。这三种标准在这里也简单说下：
<br>&emsp;At most once: 至多一次。消息在传递时，最多会被送达一次。换一个说法就是，没什么消息可靠性保证，允许丢消息。一般都是一些对消息可靠性要求不太高的监控场景使用，比如每分钟上报一次机房温度数据，可以接受数据少量丢失。
<br>&emsp;At least once: 至少一次。消息在传递时，至少会被送达一次。也就是说，不允许丢消息，但是允许有少量重复消息出现。
<br>&emsp;Exactly once：恰好一次。消息在传递时，只会被送达一次，不允许丢失也不允许重复，这个是最高的等级。
<br>&emsp;RocketMQ和Kafka都是实现的第二个标准：At least once

<h3 id="7">7. 消息积压如何解决</h3>
7.1 解决问题的最好方法是防患于未然
<br>&emsp;消息队列Broker的处理能力可以达到每秒几万到几十万条消息，并且还可以通过水平扩展Broker实例数成倍提升处理能力，所以问题的关键在于收发两端。
<br>&emsp;发送端性能优化

* 最简单的方式自然是加机器了，通过增加生产者节点来提升性能
* 优化发送端业务代码，比如采用并发、批量发送等提升吞吐量，通过异步发送提升性能

<br>&emsp;消费端性能优化

* 如果消费速度赶不上生产速度就会造成消息积压，如果这种情况在可预知时间内，那么问题不大，因为队列本身的作用就是削峰填谷。如果一直处于这样的状态下，那就必须得解决，因此在设计系统时要保证消费速度大于生产速度。
* 可以通过同时增加分区(队列)的数量和消费实例的数量来提升消费端处理能力。注意是同时增加分区数和消费者数，因为前面说过一个消费组内的消费者对于一个分区是竞争关系。

<br>7.2 消息积压了怎么办
<br>&emsp;消息积压有很多原因，根据我自己遇到过的情况可以提供一个参考：

* 消费端宕机了：快速查找宕机原因，重启机器，但要考虑重启之后消费业务是否有必要继续执行，如果没必要可以注释掉处理逻辑直接返回成功，来达到快速泄洪
* 有消费失败的消息在一直重试：解决这个问题通过查找日志来找到错误原因，然后解决之
* 生产端来了一波大促：最好提前计算各个服务性能，提前增加消费实例，如果实在没有资源可以考虑关闭消费者机器上的一些不重要的业务来让出资源，这就是所谓系统降级

<h3 id="8">8. 如何使用异步设计提升系统性能</h3>
<br>&emsp;异步设计思路和流水线作业是同一个原理，都是想办法充分利用资源来提高运行效率。
<br>&emsp;举个例子，10个搬运工，要把货车上的货物搬进库房，如果这10个人各干各的，每个人上车搬起货物，再下车搬进库房放好，然后再返回，如此反复，这就是同步作业。同步作业的最大问题就是效率低，最主要是占着连接资源不释放，其他请求只能等待。就像这里的工人，只要有人上下车他们就得等待。
<br>&emsp;再看另外一种方式，2个人在车上专门往下送货，其他人将货物运进库房，这样就大大提高了效率，每个人不用再等待了，货很快就被搬完了，这就是异步作业。
<br>&emsp;异步的优势这么明显，那么Java里提供了哪些异步的框架供我们使用呢？
<br>&emsp;比较常用也比较流行的异步框架是CompletableFuture，该框架不仅提供了很多异步方法，还允许自定义线程池，来实现用户自己控制线程数。使用示例如下：
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1000);

    /**
     * 不需要返回值
     */
    public void test() {
        for (int i = 0; i < 10000; i++) {
            CompletableFuture.runAsync(() -> {
                // TODO sth...
            }, executorService);
        }
    }

    /**
     * 如果需要返回值，可配合使用CountDownLatch
     *
     * @return
     */
    public boolean test1() {
        CountDownLatch countDownLatch = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            CompletableFuture.runAsync(() -> {
                //TODO sth...
            }, executorService).whenComplete((result, ex) -> countDownLatch.countDown());
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
```
<h3 id="9">9. Java中的线程池和并发工具类</h3>
<br>&emsp;加这节是因为上节说的异步框架中使用了线程池以及并发控制的工具类，那么这节就详细说说其使用方式及原理。
<br>9.1 Executor架构体系
<br>9.1.1 Executor两级调度模型
<br>&emsp;我们知道线程池的出现是为了降低因不停的创建和销毁线程而带来的系统开销，在这个池子中，任务交由一个一个的线程去执行，这个机制的调度器就是Executor.
<br>&emsp;下面是Executor框架的两级调度模型<br>
![](https://github.com/PeterLu798/z_e_1.png)
<br>&emsp;从上图可以看出上层的应用程序的任务和线程的对应关系由Executor框架管理。下层的调度由操作系统内核控制，不受应用程序的控制。
<br>9.1.2 Executor的核心类图<br>
![](https://github.com/PeterLu798/z_e_2.png)
<br>9.1.3 ThreadPoolExecutor
<br>&emsp;ThreadPoolExecutor是线程池的核心实现类，是我们最常用的线程池的实现类。先来看看它的运行原理。
<br>&emsp;ThreadPoolExecutor构造函数有6个核心参数，这6个核心参数就掌控了整个线程池的运行：

* 基本核心线程数(int corePoolSize)：提交任务到线程池时，线程池就创建一个新的线程来执行，即便有空闲的基本核心线程。直到创建的线程数大于基本核心线程数时就不再创建。如果调用ThreadPoolExecutor类的prestartAllCoreThreads()方法会提前创建并启动所有基本核心线程数。
* 阻塞队列(BlockingQueue<Runnable> workQueue)：如果核心线程都在执行任务，没有空闲的，这时又有新的任务提交，线程池会将这个任务加入到阻塞队列中。阻塞队列的实现有以下几种：
<br>&emsp;ArrayBlockingQueue：一个基于数组结构的有界阻塞队列，按照FIFO原则对元素进行排序。
<br>&emsp;LinkedBlockingQueue：一个基于链表结构的阻塞队列，也按照FIFO排序元素。需要注意使用此队列的无参构造函数创建队列会创建一个初始大小为Integer.MAX_VALUE的阻塞队列。
<br>&emsp;SynchronousQueue：一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态。
<br>&emsp;PriorityBlockingQueue：一个可以指定优先级排序的队列。
* 最大核心线程数(int maximumPoolSize)：线程池允许创建的最大线程数。如果阻塞队列满了，此时还在创建任务，那么这时线程池就会判断已创建的线程数是否小于maximumPoolSize，如果小于的话，会创建新的线程执行任务。
* 空闲线程存活时间(long keepAliveTime)：这个存活时间是针对 maximumPoolSize - corePoolSize 的那部分线程的。
* 时间单位(TimeUnit unit)：空闲线程存活时间单位。
* 饱和策略(RejectedExecutionHandler handler)：前面说阻塞队列满了就会判断最大核心线程数是否再创建线程来执行任务，如果线程数量已经达到了最大核心线程数，就不会再创建线程了，这时就会使用饱和策略来处理：
<br>&emsp;new AbortPolicy(): 默认策略，直接抛出异常
<br>&emsp;new DiscardPolicy(): 丢弃这个任务
<br>&emsp;new DiscardOldestPolicy(): 丢弃队列里最近的一个任务，来执行当前任务
<br>&emsp;new CallerRunsPolicy(): 只用调用者所在线程来运行任务

<br>&emsp;了解了ThreadPoolExecutor运行原理，再来看看线程池工厂类Executors提供的几个创建线程池的方法，理解它们的区别并在使用中应注意的事项。
* Executors.newFixedThreadPool(): 使用阻塞队列LinkedBlockingQueue创建线程池，阻塞队列的大小为Integer.MAX_VALUE，相当于无界，也就是说这个队列“不会满”。因此最大核心线程数、空闲线程存活时间、饱和策略都失效。在实际应用中不推荐不加界限的使用，因为这样会造成内存溢出和Full GC，导致系统宕机。
* Executors.newSingleThreadExecutor(): 只有一个核心线程的线程池。阻塞队列也使用了LinkedBlockingQueue，队列大小为Integer.MAX_VALUE
* Executors.newCachedThreadPool(): 实现源码如下：
```java
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```
<br>&emsp;核心线程数为0，最大线程数为Integer.MAX_VALUE，允许最大空闲时间为60秒，使用SynchronousQueue阻塞队列。前面说SynchronousQueue
是一个不存储元素的阻塞队列，那么它的作用是什么呢？它的作用只是一个大自然的搬运工，它只负责把主线程提交的任务直接传递给空闲线程去执行。
<br>&emsp;这类线程池适合执行耗时较短的任务，耗时短、吞吐量高、空闲时不占用内存（相当于缓存过期策略）。
<br>9.1.4 ScheduledThreadPoolExecutor
<br>&emsp;ScheduledThreadPoolExecutor是ThreadPoolExecutor的子类，同时也实现了ScheduledExecutorService接口。其构造函数源码如下：
```java
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
    }
```
<br>&emsp;super就是其父类ThreadPoolExecutor的构造函数。理解ScheduledThreadPoolExecutor关键就是DelayedWorkQueue的实现了。
<br>&emsp;DelayedWorkQueue是一个延迟阻塞队列。延迟队列的实现本质是堆，堆又被称为优先队列(priority queue)，但它并不是队列，队列是严格的按照元素到来的顺序先进先出，
但堆并不是，堆虽然在堆底插入元素，在堆顶取出元素，但堆是按照一定的优先级排序的，并不是队列的先来后到。DelayedWorkQueue是按照任务的到期时间来排序的，
执行时间在前的排列在堆顶（或者可以理解为优先队列的头部）时间在后的排列在堆底（也可以理解为优先队列的尾部），当阻塞在DelayedWorkQueue上的工作线程尝试
取出任务时，会判断执行时间是否到期，也就是 任务执行时间-now() 是否小于等于0，如果是则将该任务出队，否则返回null。
<br>9.1.5 Executor可作为FutureTask的执行器
<br>&emsp;FutureTask实现了Future接口，主要用来做异步计算以及超时限定。比如很多时候我们需要调用一个第三方接口，我们无法保证这个第三方接口的性能，但是我们需要控制我们
自己程序的性能，这时就需要对第三方接口做超时限定，如果在规定时间内无返回我们需要做额外的逻辑计算。示例如下：
```java
public class ThirdInterface {
    /**
     *
     * @return
     */
    public String excuteCrud(){
        return "ok";
    }
}
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
```
<br>9.2 阻塞队列
<br>&emsp;线程池的实现离不开阻塞队列。阻塞队列和我们平常使用的普通队列比如ArrayList、LinkedList最大的不同在于阻塞队列的阻塞添加和阻塞删除方法。
* 阻塞添加：当队列满了的时候，队列会阻塞插入元素的线程，直到队列不满。
* 阻塞删除：当队列为空时，获取元素的线程会等待队列变为非空。
<br>&emsp;下面先从整体上了解下阻塞队列的规范。Java里阻塞队列定义的规范接口是BlockingQueue，从这个接口入手。
```java
public interface BlockingQueue<E> extends Queue<E> {
    /**
     * 将元素插入队列，如果成功立即返回true，如果队列满则直接抛出IllegalStateException异常
     */
    boolean add(E e);

    /**
     * 插入元素到队尾，如果成功返回true，失败返回false。
     * 该方法在用于有界队列时优于add方法
     */
    boolean offer(E e);

    /**
     * 插入元素到队尾，如果队列满则阻塞等待
     */
    void put(E e) throws InterruptedException;

    /**
     * 将元素插入队尾，成功返回true，如果队列满则在指定的时间内等待队列能否变为不满
     */
    boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException;

    /**
     * 检索并删除队首的元素，会阻塞等待，直到元素可用
     */
    E take() throws InterruptedException;

    /**
     * 检索并删除队首的元素，成功的话返回元素，失败则会在指定的时间内等待一个可用的元素，否则返回null
     */
    E poll(long timeout, TimeUnit unit)
            throws InterruptedException;
    /**
     * 如果此元素存在于队列中，删除其中一个实例。成功返回true，失败返回false
     */
    boolean remove(Object o);
}
```
<br>&emsp;简单总结就是非阻塞添加和删除方法为：add、offer、poll、remove，它们都有o；阻塞添加和删除方法为：put、take它们都有t。
<br>&emsp;要想实现阻塞方法，就得借助通知模式了。所谓通知模式，就是当生产者往满的队列里添加元素时会阻塞住生产者，当消费者消费了一个队列中的元素后，
会通知生产者当前队列可用。当然消费者也要借助生产者添加元素时的通知，当消费者尝试消费一个空的队列时，一样也会阻塞等待，直到生产者往队列中插入了一个元素，
就会通知消费者可以消费了。
<br>&emsp;接下来简单总结几种典型的非阻塞队列的实现。
* ArrayBlockingQueue 使用一个可重入锁(lock = new ReentrantLock())和两个条件(notEmpty = lock.newCondition(), notFull = lock.newCondition())
在添加元素时，不但要获取lock锁还需要满足notFull条件，换言之添加元素时必须获得全局锁以及队列不能满，否则一直等待：notFull.await(); 删除元素时，也要获取全局锁lock，并且还要满足notEmpty条件，如果队列为空，则一直等待：notEmpty.await();
<br>&emsp;添加的notFull条件在删除方法中被通知：notFull.signal(); 同样删除的notEmpty条件在添加方法中被通知：notEmpty.signal(); 这样就实现了阻塞队列。
* LinkedBlockingQueue 和ArrayBlockingQueue的区别之一是它使用了两把锁，一把是在添加元素时使用的锁，一把是在删除元素时使用。也就是说LinkedBlockingQueue的添加和删除不会竞争同一把锁，这也是为什么LinkedBlockingQueue的吞吐量要高于ArrayBlockingQueue的一个原因。
* SynchronousQueue 是一个没有数据缓冲的BlockingQueue，生产者线程对其的插入操作put必须等待消费者的移除操作take，反过来也一样。不像ArrayBlockingQueue或LinkedListBlockingQueue，SynchronousQueue内部并没有数据缓存空间，你不能调用peek()方法来看队列中是否有数据元素，因为数据元素只有当你试着取走的时候才可能存在，不取走而只想偷窥一下是不行的，当然遍历这个队列的操作也是不允许的。队列头元素是第一个排队要插入数据的线程，而不是要交换的数据。数据是在配对的生产者和消费者线程之间直接传递的，并不会将数据缓冲数据到队列中。可以这样来理解：生产者和消费者互相等待对方，握手，然后一起离开。
* DelayQueue 它的实现是借助于优先队列，也就是堆，和DelayedWorkQueue的实现原理差不多。
<br>9.3 Java中的并发工具
<br>9.3.1 CountDownLatch CountDownLatch 是一个同步工具类，它允许一个或多个线程一直等待，直到其他线程的操作执行完后再执行。CountDownLatch是通过一个计数器来实现的，计数器的初始值为并发的线程的数量，这个数量在构造函数指定。每当一个线程完成了自己的任务后，计数器的值就会减1。当计数器值到达0时，它表示所有的线程已经完成了任务，然后在闭锁上等待的线程就可以恢复执行任务。
<br>9.3.2 CyclicBarrier CyclicBarrier的字面意思是可循环使用(Cyclic)的屏障(Barrier)。它要做的事情是，让一组线程到达一个屏障时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续执行。
<br>&emsp;CyclicBarrier使用例子
```java
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
}
```
运行结果为
<br>1
<br>2
<br>或者
<br>2
<br>1
<br>&emsp;如果把new CyclicBarrier(2)修改成new CyclicBarrier(3)则主线程和子线程会永远等待，因为没有第三个线程执行await方法，即没有第三个线程到达屏障，所以之前到达屏障的两个线程都不会继续执行。
<br>&emsp;CyclicBarrier还提供一个更高级的构造函数CyclicBarrier(int parties, Runnable barrierAction)，用于在线程到达屏障时，优先执行barrierAction，方便处理更复杂的业务场景。代码如下：
```java
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
```
运行结果为
<br>3
<br>1
<br>2
<br>&emsp;CyclicBarrier和CountDownLatch的区别
* CountDownLatch的计数器只能使用一次。而CyclicBarrier的计数器可以使用reset() 方法重置。所以CyclicBarrier能处理更为复杂的业务场景，比如如果计算发生错误，可以重置计数器，并让线程们重新执行一次。
* CyclicBarrier还提供其他有用的方法，比如getNumberWaiting方法可以获得CyclicBarrier阻塞的线程数量。isBroken方法用来知道阻塞的线程是否被中断。

<br>9.3.3 Semaphore Semaphore翻译为信号量。一般用来做流量限制。Semaphore可以通过其构造函数Semaphore(int permits)创建指定个“许可证书”，然后通过acquire()获取许可证，通过release()归还许可证，这样只允许能达到许可证的线程执行资源，来达到限流的作用。
具体示例如如下：有30个线程，但是只有10个许可，因此每次只允许10个线程并发执行。
```java
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
```
<br>9.3.4 Exchanger 译为交换者。交换谁呢？交换两个线程的数据。它提供一个同步点，在这个同步点，两个线程可以交换彼此的数据。
这两个线程通过exchange方法交换数据，如果第一个线程先执行exchange()方法，它会一直等待第二个线程也执行exchange()方法，当两个
线程都到达同步点时，就可以彼此交换数据。示例代码如下：
```java
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
```
<br>&emsp;
<br>&emsp;
<br>&emsp;
<br>






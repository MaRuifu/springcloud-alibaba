## 高并发带来的问题 

在微服务架构中，我们将业务拆分成一个个的服务，服务与服务之间可以相互调用，但是由于网络原因 或者自身的原因，服务并不能保证服务的100%可用，如果单个服务出现问题，调用这个服务就会出现 网络延迟，此时若有大量的网络涌入，会形成任务堆积，最终导致服务瘫痪。

**接下来，我们来模拟一个高并发的场景**

1. 编写java代码

```
@RestController
@Slf4j
public class OrderController2 {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;

    @RequestMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info("接收到{}号商品的下单请求,接下来调用商品微服务查询此商品信息", pid);//调用商品微服 务,查询商品信息
        Product product = productService.findByPid(pid);
        log.info("查询到{}号商品的信息,内容是:{}", pid, JSON.toJSONString(product)); //模拟一次网络延时
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //下单(创建订单)
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(pid);
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        //为了不产生太多垃圾数据,暂时不做订单保存
        //orderService.createOrder(order);
        log.info("创建订单成功,订单信息为{}", JSON.toJSONString(order));
        return order;
    }

    @RequestMapping("/order/message")
    public String message() {
        return "高并发下的问题测试";
    }
}
 

```

2. 修改配置文件中tomcat的并发数

```
server:
 #端口
 port: 8091
 #tomcat配置
 tomcat:
  #tomcat的最大并发值修改为10,默认是200
 	max-threads: 10 #tomcat的最大并发值修改为10,默认是200
```

3. 接下来使用压测工具,对请求进行压力测试

下载地址https://jmeter.apache.org/

第一步:修改配置，并启动软件

进入bin目录,修改jmeter.properties文件中的语言支持为language=zh_CN，然后点击jmeter.bat 启动 软件。

​				 			 			 		

![压测工具](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.29.58.png)

第二步:添加线程组

![添加线程组](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.30.30.png)

第三步:配置线程并发数

![配置线程并发数](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.31.22.png)

第四步:添加Http取样

![添加Http取样](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.31.44.png)

第五步:配置取样，并启动测试

![配置取样，并启动测试](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.32.12.png)

4. 访问message方法观察效果

**结论**:
此时会发现, 由于order方法囤积了大量请求, 导致message方法的访问出现了问题，这就是服务雪崩的 雏形。

## 服务雪崩效应

在分布式系统中,由于网络原因或自身的原因,服务一般无法保证 100% 可用。如果一个服务出现了问 题，调用这个服务就会出现线程阻塞的情况，此时若有大量的请求涌入，就会出现多条线程阻塞等待， 进而导致服务瘫痪。

由于服务与服务之间的依赖性，故障会传播，会对整个微服务系统造成灾难性的严重后果，这就是服务 故障的 **雪崩效应** 。

![服务雪崩效应](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.33.48.png)

雪崩发生的原因多种多样，有不合理的容量设计，或者是高并发下某一个方法响应变慢，亦或是某台机 器的资源耗尽。我们无法完全杜绝雪崩源头的发生，只有做好足够的容错，保证在一个服务发生问题， 不会影响到其它服务的正常运行。也就是"雪落而不雪崩"。

## 常见容错方案

要防止雪崩的扩散，我们就要做好服务的容错，容错说白了就是保护自己不被猪队友拖垮的一些措施,

下面介绍常见的服务容错思路和组件。

**常见的容错思路**

常见的容错思路有隔离、超时、限流、熔断、降级这几种，下面分别介绍一下。

- **隔离**

它是指将系统按照一定的原则划分为若干个服务模块，各个模块之间相对独立，无强依赖。当有故障发 生时，能将问题和影响隔离在某个模块内部，而不扩散风险，不波及其它模块，不影响整体的系统服 务。常见的隔离方式有:线程池隔离和信号量隔离.

![隔离](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.35.47.png)

- **超时**

在上游服务调用下游服务的时候，设置一个最大响应时间，如果超过这个时间，下游未作出反应，就断 开请求，释放掉线程。

![超时](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.37.36.png)

- **限流**

限流就是限制系统的输入和输出流量已达到保护系统的目的。为了保证系统的稳固运行,一旦达到的需要 限制的阈值,就需要限制流量并采取少量措施以完成限制流量的目的。

![限流](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.38.07.png)

- **熔断**

在互联网系统中，当下游服务因访问压力过大而响应变慢或失败，上游服务为了保护系统整体的可 用性，可以暂时切断对下游服务的调用。这种牺牲局部，保全整体的措施就叫做熔断。

![熔断**](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.39.43.png)

**服务熔断一般有三种状态:**

- 熔断关闭状态(Closed) 

服务没有故障时，熔断器所处的状态，对调用方的调用不做任何限制

- 熔断开启状态(Open)

 后续对该服务接口的调用不再经过网络，直接执行本地的fallback方法

- 半熔断状态(Half-Open) 

尝试恢复服务调用，允许有限的流量调用该服务，并监控调用成功率。如果成功率达到预期，则说明服务已恢复，进入熔断关闭状态;如果成功率仍旧很低，则重新进入熔断关闭状态。

- **降级**

降级其实就是为服务提供一个托底方案，一旦服务无法正常调用，就使用托底方案。

 ![降级](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-8.40.48.png)

常见的容错组件

- Hystrix

  Hystrix是由Netflflix开源的一个延迟和容错库，用于隔离访问远程系统、服务或者第三方库，防止级联 失败，从而提升系统的可用性与容错性。

- Resilience4J
  Resilicence4J一款非常轻量、简单，并且文档非常清晰、丰富的熔断工具，这也是Hystrix官方推荐的替 代产品。不仅如此，Resilicence4j还原生支持Spring Boot 1.x/2.x，而且监控也支持和prometheus等 多款主流产品进行整合。

- Sentinel
  Sentinel 是阿里巴巴开源的一款断路器实现，本身在阿里内部已经被大规模采用，非常稳定。

**下面是三个组件在各方面的对比:**

|                 | Sentinel                                                   | Hystrix                |           resilience4j           |
| :-------------: | ---------------------------------------------------------- | ---------------------- | :------------------------------: |
|    隔离策略     | 信号量隔离(并发线程数限流)                                 | 线程池隔离/信号量隔离  |            信号量隔离            |
|  熔断降级策略   | 基于响应时间、异常比率、异常数                             | 基于异常比率           |        基于异常比率、响应        |
|  实时统计实现   | 滑动窗口(LeapArray)                                        | 滑动窗口(基 于 RxJava) |         Ring Bit Buffer          |
|  动态规则配置   | 支持多种数据源                                             | 支持多种数据           |             有限支持             |
|     扩展性      | 多个扩展点                                                 | 插件的形式             |            接口的形式            |
| 基于注解的支 持 | 支持                                                       | 支持                   |               支持               |
|      限流       | 基于 QPS，支持基于调用关系的限流                           | 有限的支持             |           Rate Limiter           |
|    流量整形     | 支持预热模式、匀速器模式、预热排队模式                     | 不支持                 |     简单的 Rate Limiter模式      |
| 系统自适应保护  | 支持                                                       | 不支持                 |              不支持              |
|     控制台      | 提供开箱即用的控制台，可配置规则、查看秒级监控、机器发现等 | 简单的监控查看         | 不提供控制台，可对接其它监控系统 |

## Sentinel入门

### 什么是Sentine 

Sentinel (分布式系统的流量防卫兵) 是阿里开源的一套用于**服务容错**的综合性解决方案。它以流量 为切入点, 从**流量控制、熔断降级、系统负载保护**等多个维度来保护服务的稳定性。

**Sentinel** **具有以下特征**

- **丰富的应用场景**:Sentinel 承接了阿里巴巴近 10 年的双十一大促流量的核心场景, 例如秒杀(即 突发流量控制在系统容量可以承受的范围)、消息削峰填谷、集群流量控制、实时熔断下游不可用 应用等。

- **完备的实时监控**:Sentinel 提供了实时的监控功能。通过控制台可以看到接入应用的单台机器秒 级数据, 甚至 500 台以下规模的集群的汇总运行情况。

- **广泛的开源生态**:Sentinel 提供开箱即用的与其它开源框架/库的整合模块, 例如与 SpringCloud、 Dubbo、gRPC 的整合。只需要引入相应的依赖并进行简单的配置即可快速地接入Sentinel。 

- **完善的SPI扩展点**:Sentinel 提供简单易用、完善的 SPI 扩展接口。您可以通过实现扩展接口来快 速地定制逻辑。例如定制规则管理、适配动态数据源等。

**Sentinel分为两个部分**:

-  核心库(Java 客户端)不依赖任何框架/库,能够运行于所有 Java 运行时环境，同时对 Dubbo/Spring Cloud 等框架也有较好的支持。
- 控制台(Dashboard)基于 Spring Boot 开发，打包后可以直接运行，不需要额外的 Tomcat 等 应用容器。

### 微服务集成Sentinel 

为微服务集成Sentinel非常简单, 只需要加入Sentinel的依赖即可

1 在pom.xml中加入下面依赖

```
<dependency>
	<groupId>com.alibaba.cloud</groupId> 
	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId> 
</dependency>
```

2 编写一个Controller测试使用



```
package cn.maruifu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//
@RestController
@Slf4j
public class OrderTestController {

    @RequestMapping("/order/message1")
    public String message1() {
        return "message1";
    }

    @RequestMapping("/order/message2")
    public String message2() {
        return "message2";
    }
}

```

### 安装Sentinel控制台

Sentinel 提供一个轻量级的控制台, 它提供机器发现、单机资源实时监控以及规则管理等功能。

1. 下载jar包,解压到文件夹
    https://github.com/alibaba/Sentinel/releases

2 .启动控制台

```
 # 直接使用jar命令启动项目(控制台本身是一个SpringBoot项目)
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard-1.8.3.jar
```

3. 修改 shop-order ,在里面加入有关控制台的配置

```
spring:
  cloud:
    sentinel:
      transport:
        port: 9999 #跟控制台交流的端口,随意指定一个未使用的端口即可 
        dashboard: localhost:8080 # 指定控制台服务的地址

```

4. 通过浏览器访问localhost:8080 进入控制台 ( 默认用户名密码是 sentinel/sentinel )
5. ![控制台](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.02.08.png)

> 控制台的使用原理Sentinel的控制台其实就是一个SpringBoot编写的程序。我们需要将我们的微服务程序注册到控制台上, 即在微服务中指定控制台的地址, 并且还要开启一个跟控制台传递数据的端口, 控制台也可以通过此端口 调用微服务中的监控程序获取微服务的各种信息。

![控制台](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.03.02.png)



### 实现一个接口的限流 

通过控制台为message1添加一个流控规则

![流控规则](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.03.30.png)

通过控制台快速频繁访问, 观察效果

![控制台快速频繁访问](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.03.59.png)

## Sentinel的概念和功能 
### 基本概念

- **资源**

**资源就是Sentinel要保护的东西**

资源是 Sentinel 的关键概念。它可以是 Java 应用程序中的任何内容，可以是一个服务，也可以是一个 方法，甚至可以是一段代码。

> 我们入门案例中的message1方法就可以认为是一个资源

- **规则**

**规则就是用来定义如何进行保护资源的**

作用在资源之上, 定义以什么样的方式保护资源，主要包括流量控制规则、熔断降级规则以及系统保护 规则。

> 我们入门案例中就是为message1资源设置了一种流控规则, 限制了进入message1的流量

###  重要功能

![重要功能](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.06.14.png)

Sentinel的主要功能就是容错，主要体现为下面这三个: 

- **流量控制**

流量控制在网络传输中是一个常用的概念，它用于调整网络包的数据。任意时间到来的请求往往是随机 不可控的，而系统的处理能力是有限的。我们需要根据系统的处理能力对流量进行控制。Sentinel 作为 一个调配器，可以根据需要把随机的请求调整成合适的形状。

- **熔断降级**

当检测到调用链路中某个资源出现不稳定的表现，例如请求响应时间长或异常比例升高的时候，则对这 个资源的调用进行限制，让请求快速失败，避免影响到其它的资源而导致级联故障

**Sentinel 对这个问题采取了两种手段:** 

- 通过并发线程数进行限制

Sentinel 通过限制资源并发线程的数量，来减少不稳定资源对其它资源的影响。当某个资源出现不稳定 的情况下，例如响应时间变长，对资源的直接影响就是会造成线程数的逐步堆积。当线程数在特定资源 上堆积到一定的数量之后，对该资源的新请求就会被拒绝。堆积的线程完成任务后才开始继续接收请 求。

- 通过响应时间对资源进行降级

除了对并发线程数进行控制以外，Sentinel 还可以通过响应时间来快速降级不稳定的资源。当依赖的资 源出现响应时间过长后，所有对该资源的访问都会被直接拒绝，直到过了指定的时间窗口之后才重新恢复。

**Sentinel和 Hystrix的区别**

> 两者的原则是一致的, 都是当一个资源出现问题时, 让其快速失败, 不要波及到其它服务 但是在限制的手段上, 确采取了完全不一样的方法:
>
> Hystrix 采用的是线程池隔离的方式, 优点是做到了资源之间的隔离, 缺点是增加了线程切换 的成本。
>
> Sentinel 采用的是通过并发线程的数量和响应时间来对资源做限制。

- 系统负载保护
   Sentinel 同时提供系统维度的自适应保护能力。当系统负载较高的时候，如果还持续让请求进入 可能会导致系统崩溃，无法响应。在集群环境下，会把本应这台机器承载的流量转发到其它的机器 上去。如果这个时候其它的机器也处在一个边缘状态的时候，Sentinel 提供了对应的保护机制， 让系统的入口流量和系统的负载达到一个平衡，保证系统在能力范围之内处理最多的请求

**总之一句话:我们需要做的事情，就是在Sentinel的资源上配置各种各样的规则，来实现各种容错的功能。**

## Sentinel规则

流量控制，其原理是监控应用流量的QPS(每秒查询率) 或并发线程数等指标，当达到指定的阈值时对流 量进行控制，以避免被瞬时的流量高峰冲垮，从而保障应用的高可用性。

第1步: 点击簇点链路，我们就可以看到访问过的接口地址，然后点击对应的流控按钮，进入流控规则配 置页面。新增流控规则界面如下

### 流控规则 

![image-20220217152248149](https://nas.mrf.ink:10001/images/2022/02/17/image-20220217152248149.png)



**资源名**:唯一名称，默认是请求路径，可自定义

**针对来源**:指定对哪个微服务进行限流，默认指default，意思是不区分来源，全部限制

**阈值类型单机阈值**:

- QPS(每秒请求数量): 当调用该接口的QPS达到阈值的时候，进行限流 

- 线程数:当调用该接口的线程数达到阈值的时候，进行限流

**是否集群**:暂不需要集群 接下来我们以QPS为例来研究限流规则的配置。



#### 简单配置

我们先做一个简单配置，设置阈值类型为QPS，单机阈值为3。即每秒请求量大于3的时候开始限流。接下来，在流控规则页面就可以看到这个配置。

![简单配置](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.10.45.png)

#### 配置流控模式

![配置流控模式](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.11.05.png)

**sentinel共有三种流控模式，分别是:**

- 直接(默认):接口达到限流条件时，开启限流 

- 关联:当关联的资源达到限流条件时，开启限流 [适合做应用让步] 

- 链路:当从某个接口过来的资源达到限流条件时，开启限流

下面呢分别演示三种模式:

**直接流控模式**

直接流控模式是最简单的模式，当指定的接口达到限流条件时开启限流。上面案例使用的就是直接流控 模式。

**关联流控模式**

关联流控模式指的是，当指定接口关联的接口达到限流条件时，开启对指定接口开启限流。第1步:配 置限流规则,将流控模式设置为关联，关联资源设置为的 /order/message2。

![关联流控模式](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.12.17.png)

通过Jmeter软件向/order/message2连续发送请求，注意QPS一定要大于3

![截屏2021-01-29 下午9.13.31](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.13.31.png)

![截屏2021-01-29 下午9.13.48](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.13.48.png)

访问/order/message1,会发现已经被限流

![截屏2021-01-29 下午9.14.06](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.14.06.png)

**链路流控模式**

链路流控模式指的是，当从某个接口过来的资源达到限流条件时，开启限流。它的功能有点类似于针对 来源配置项，区别在于:**针对来源是针对上级微服务，而链路流控是针对上级接口，也就是说它的粒度 更细。**

1.编写一个service，在里面添加一个方法message

```

package cn.maruifu.service.impl;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class OrderTestServiceImpl {
    @SentinelResource("message")
    public void message() {
        System.out.println("message");
    }
}
```



第2步: 在Controller中声明两个方法，分别调用service中的方法message

```
package cn.maruifu.controller;

import cn.maruifu.service.impl.OrderTestServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//
@RestController
@Slf4j
public class OrderTestController {

    /*

    @RequestMapping("/order/message1")
    public String message1() {
        return "message1";
    }

    @RequestMapping("/order/message2")
    public String message2() {
        return "message2";
    }*/



    @Autowired
    private OrderTestServiceImpl orderTestServiceImpl;

    @RequestMapping("/order/message1")
    public String message1() {
        orderTestServiceImpl.message();
        return "message1";
    }
    @RequestMapping("/order/message2")
    public String message2() {
        orderTestServiceImpl.message();
        return "message2";
    } 
}

```

第3步: 禁止收敛URL的入口 context

> 1.6.3 版本开始，Sentinel Web fifilter默认收敛所有URL的入口context，因此链路限流不生效。
>        1.7.0 版本开始(对应SCA的2.1.1.RELEASE)，官方在CommonFilter 引入了WEB_CONTEXT_UNIFY 参数， 用于控制是否收敛context。将其配置为 false 即可根据不同的URL进行链路限流。
>
> SCA 2.1.1.RELEASE之后的版本,可以通过配置spring.cloud.sentinel.web-context-unify=false即 可关闭收敛我们当前使用的版本是SpringCloud Alibaba 2.1.0.RELEASE，无法实现链路限流。
>
> 目前官方还未发布SCA 2.1.2.RELEASE，所以我们只能使用2.1.1.RELEASE，需要写代码的形式实 现

**(1)** 暂时将SpringCloud Alibaba的版本调整为2.1.1.RELEASE

```
<spring-cloud-alibaba.version>2.1.1.RELEASE</spring-cloud-alibaba.version>
```

**(2)** 配置文件中关闭sentinel的CommonFilter实例化

```

spring:
  cloud:
    sentinel:
      filter:
       enabled: false
```

**(3)** 添加一个配置类，自己构建CommonFilter实例

```
package cn.maruifu.config;
import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; @Configuration
public class FilterContextConfig {
    @Bean
    public FilterRegistrationBean sentinelFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CommonFilter());
        registration.addUrlPatterns("/*");
         // 入口资源关闭聚合
        registration.addInitParameter(CommonFilter.WEB_CONTEXT_UNIFY, "false");
        registration.setName("sentinelFilter");
        registration.setOrder(1);
        return registration;
    }
}

 
```

**(4)** : 控制台配置限流规则

![控制台配置限流规则](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.19.22.png)

**(5)** :分别通过 /order/message1 和 /order/message2 访问, 发现2没问题, 1的被限流了



#### 配置流控效果 

- **快速失败(默认)**: 直接失败，抛出异常，不做任何额外的处理，是最简单的效果

-  **Warm Up**:它从开始阈值到最大QPS阈值会有一个缓冲阶段，一开始的阈值是最大QPS阈值的 1/3，然后慢慢增长，直到最大阈值，适用于将突然增大的流量转换为缓步增长的场景。 

- **排队等待**:让请求以均匀的速度通过，单机阈值为每秒通过数量，其余的排队等待; 它还会让设 置一个超时时间，当请求超过超时间时间还未处理，则会被丢弃

### 降级规则

**降级规则就是设置当满足什么条件的时候，对服务进行降级。****Sentinel****提供了三个衡量条件:**

- 平均响应时间 :当资源的平均响应时间超过阈值(以 ms 为单位)之后，资源进入准降级状态。 如果接下来 1s 内持续进入 5 个请求，它们的 RT都持续超过这个阈值，那么在接下的时间窗口 (以 s 为单位)之内，就会对这个方法进行服务降级。
- ![平均响应时间](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.20.48.png)

> 注意 Sentinel 默认统计的 RT 上限是 4900 ms，超出此阈值的都会算作 4900 ms，若需要变更此 上限可以通过启动配置项 -Dcsp.sentinel.statistic.max.rt=xxx 来配置。

- 异常比例:当资源的每秒异常总数占通过量的比值超过阈值之后，资源进入降级状态，即在接下的 时间窗口(以 s 为单位)之内，对这个方法的调用都会自动地返回。异常比率的阈值范围是 [0.0,1.0]。

第1步: 首先模拟一个异常

```
int i=0;

@RequestMapping("/order/message2") 
public String message2(){
    i++;
    //异常比例为0.333
    if(i%3==0){
        throw new RuntimeException();
    }
    return"message2";
}

```

第2步: 设置异常比例为0.25

![置异常比例为0.25](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.22.30.png)



- 异常数 :当资源近 1 分钟的异常数目超过阈值之后会进行服务降级。注意由于统计时间窗口是分 钟级别的，若时间窗口小于 60s，则结束熔断状态后仍可能再进入熔断状态。

![截屏2021-01-29 下午9.22.58](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.22.58.png)

> 问题:流控规则和降级规则返回的异常页面是一样的，我们怎么来区分到底是什么原因导致的 呢?

### 热点规则 

热点参数流控规则是一种更细粒度的流控规则, 它允许将规则具体到参数上。 **热点规则简单使用**

第1步: 编写代码

```
@RequestMapping("/order/message3") 
@SentinelResource("message3")
//注意这里必须使用这个注解标识,热点规则不生效 
public String message3(String name, Integer age) {
    return name + age;
}
```
第2步: 配置热点规则
![热点规则 ](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.25.02.png)

第3步: 分别用两个参数访问,会发现只对第一个参数限流了

![截屏2021-01-29 下午9.26.01](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.26.01.png)

**热点规则增强使用**

参数例外项允许对一个参数的具体值进行流控 

编辑刚才定义的规则,增加参数例外项

![热点规则增强使用](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.26.34.png)



### 授权规则 

很多时候，我们需要根据调用来源来判断该次请求是否允许放行，这时候可以使用 Sentinel 的来源访问控制的功能。来源访问控制根据资源的请求来源(origin)限制资源是否通过: 

- 若配置白名单，则只有请求来源位于白名单内时才可通过;

- 若配置黑名单，则请求来源位于黑名单时不通过，其余的请求通过。
- ![截屏2021-01-29 下午9.27.10](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.27.10.png)

**上面的资源名和授权类型不难理解，但是流控应用怎么填写呢?**

> 其实这个位置要填写的是来源标识，Sentinel提供了 RequestOriginParser 接口来处理来源。 
>
> 只要Sentinel保护的接口资源被访问，Sentinel就会调用 RequestOriginParser 的实现类去解析访问来源。

第1步: 自定义来源处理规则

```
@Component
public class RequestOriginParserDefinition implements RequestOriginParser{ @Override
public String parseOrigin(HttpServletRequest request) {
    String serviceName = request.getParameter("serviceName");
    return serviceName;
}
}
```

第2步: 授权规则配置 

这个配置的意思是只有serviceName=pc不能访问(黑名单)

![授权规则配置](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.28.28.png)

第3步: 访问 http://localhost:8091/order/message1?serviceName=pc观察结果

### 系统规则

系统保护规则是从应用级别的入口流量进行控制，从单台机器的总体 Load、RT、入口 QPS 、CPU使用 率和线程数五个维度监控应用数据，让系统尽可能跑在最大吞吐量的同时保证系统整体的稳定性。系统 保护规则是应用整体维度的，而不是资源维度的，并且仅对入口流量 (进入应用的流量) 生效。

- Load(仅对 Linux/Unix-like 机器生效):当系统 load1 超过阈值，且系统当前的并发线程数超过 系统容量时才会触发系统保护。系统容量由系统的 maxQps * minRt 计算得出。设定参考值一般 是 CPU cores * 2.5。

-  RT:当单台机器上所有入口流量的平均 RT 达到阈值即触发系统保护，单位是毫秒。 

- 线程数:当单台机器上所有入口流量的并发线程数达到阈值即触发系统保护。

- 入口 QPS:当单台机器上所有入口流量的 QPS 达到阈值即触发系统保护。 

- CPU使用率:当单台机器上所有入口流量的 CPU使用率达到阈值即触发系统保护

**扩展:自定义异常返回**

```
//异常处理页面
@Component
public class ExceptionHandlerPage implements UrlBlockHandler {
    //用于定义资源，并提供可选的异常处理和 fallback 配置项。其主要参数如下:
    @SentinelResource

    /**
     *  BlockException 异常接口,包含Sentinel的五个异常
     *  FlowException 限流异常
     *  DegradeException 降级异常
     *  ParamFlowException 参数限流异常
     *  AuthorityException 授权异常
     *  SystemBlockException 系统负载异常
     *
     */
    @Override
    public void blocked(HttpServletRequest request, HttpServletResponse
            response, BlockException e) throws IOException { response.setContentType("application/json;charset=utf-8");
        ResponseData data = null;
        if (e instanceof FlowException) {
            data = new ResponseData(-1, "接口被限流了...");
        } else if (e instanceof DegradeException) {
            data = new ResponseData(-2, "接口被降级了...");
        }
        response.getWriter().write(JSON.toJSONString(data));
    }
}
    @Data 
    //全参构造
    @AllArgsConstructor
    //无参构造
    @NoArgsConstructor
    class ResponseData {
        private int code;
        private String message;
    }
}

```

## @SentinelResource的使用 

在定义了资源点之后，我们可以通过Dashboard来设置限流和降级策略来对资源点进行保护。同时还能通过@SentinelResource来指定出现异常时的处理策略。

 @SentinelResource 用于定义资源，并提供可选的异常处理和 fallback 配置项。其主要参数如下:

|        属性        |                             作用                             |
| :----------------: | :----------------------------------------------------------: |
|       value        |                           资源名称                           |
|     entryType      |       entry类型，标记流量的方向，取值IN/OUT，默认是OUT       |
|    blockHandler    | 处理BlockException的函数名称,函数要求:1. 必须是 public2. 返回类型 参数与原方法一致3. 默认需和原方法在同一个类中。若希望使用其他类 的函数，可配置blockHandlerClass ，并指定blockHandlerClass里面的 方法。 |
| blockHandlerClass  |     存放blockHandler的类,对应的处理函数必须static修饰。      |
|      fallback      | 用于在抛出异常的时候提供fallback处理逻辑。fallback函数可以针对所 有类型的异常(除了exceptionsToIgnore 里面排除掉的异常类型)进行 处理。函数要求:1. 返回类型与原方法一致2. 参数类型需要和原方法相 匹配3. 默认需和原方法在同一个类中。若希望使用其他类的函数，可配 置fallbackClass ，并指定fallbackClass里面的方法。 |
|   fallbackClass    |        存放fallback的类。对应的处理函数必须static修饰        |
|  defaultFallback   | 用于通用的 fallback 逻辑。默认fallback函数可以针对所有类型的异常进 行处理。若同时配置了 fallback 和 defaultFallback，以fallback为准。函 数要求:1. 返回类型与原方法一致2. 方法参数列表为空，或者有一个 Throwable 类型的参数。3. 默认需要和原方法在同一个类中。若希望使 用其他类的函数，可配置fallbackClass ，并指定 fallbackClass 里面的方 法。 |
| exceptionsToIgnore | 指定排除掉哪些异常。排除的异常不会计入异常统计，也不会进入 fallback逻辑，而是原样抛出 |
| exceptionsToTrace  |                       需要trace的异常                        |

**定义限流和降级后的处理方法**

方式一:直接将限流和降级方法定义在方法中

```
@Service
@Slf4j
public class OrderServiceImpl3 {
    int i = 0;
    @SentinelResource(value = "message",  /*指定发生BlockException时进入的方法*/
    blockHandler = "blockHandler", /* 指定发生Throwable时进入的方法) */ 
    fallback = "fallback"
    public String message(){
        i++;
        if(i % 3 == 0){
          throw new RuntimeException();
        }
         return"message";
     }
         
         
     //BlockException时进入的方法
     public String blockHandler(BlockException ex){
        log.error("{}", ex);
        return"接口被限流或者降级了...";
    }

    //Throwable时进入的方法
    public String fallback(Throwable throwable) {
        log.error("{}", throwable);
        return "接口发生异常了...";
    }
}

```

 方式二: 将限流和降级方法外置到单独的类中

 ```
@Service
@Slf4j
public class OrderServiceImpl3 {
    int i = 0;
    @SentinelResource(  value = "message", blockHandlerClass = OrderServiceImpl3BlockHandlerClass.class, blockHandler = "blockHandler", fallbackClass = OrderServiceImpl3FallbackClass.class, fallback = "fallback"  )
    public String message() {
        i++;
        if (i % 3 == 0) {
            throw new RuntimeException();
        }
        return "message4";
    }
}
@Slf4j
public class OrderServiceImpl3BlockHandlerClass { //注意这里必须使用static修饰方法
    public static String blockHandler(BlockException ex) { 1 编写处理类
        log.error("{}", ex);
        return "接口被限流或者降级了...";
    }
}
@Slf4j
public class OrderServiceImpl3FallbackClass { //注意这里必须使用static修饰方法
    public static String fallback(Throwable throwable) { log.error("{}", throwable);
        return "接口发生异常了...";
    }
}
 
 ```

## Sentinel规则持久化

通过前面的讲解，我们已经知道，可以通过Dashboard来为每个Sentinel客户端设置各种各样的规则，

但是这里有一个问题，就是这些规则默认是存放在内存中，极不稳定，所以需要将其持久化。 本地文件数据源会定时轮询文件的变更，读取规则。这样我们既可以在应用本地直接修改文件来更新规则，也可以通过 Sentinel 控制台推送规则。以本地文件数据源为例，推送过程如下图所示:

![截屏2021-01-29 下午9.42.57](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.42.57.png)



首先 Sentinel 控制台通过 API 将规则推送至客户端并更新到内存中，接着注册的写数据源会将新的规则 保存到本地的文件中。

 编写处理类

```
package cn.maruifu.config;

import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler;
import com.alibaba.csp.sentinel.datasource.*;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;

//规则持久化
public class FilePersistence implements InitFunc {
    @Value("spring.application:name")
    private String appcationName;

    @Override
    public void init() throws Exception {
        String ruleDir = System.getProperty("user.home") + "/sentinel-rules/" + appcationName;
        String flowRulePath = ruleDir + "/flow-rule.json";
        String degradeRulePath = ruleDir + "/degrade-rule.json";
        String systemRulePath = ruleDir + "/system-rule.json";
        String authorityRulePath = ruleDir + "/authority-rule.json";
        String paramFlowRulePath = ruleDir + "/param-flow-rule.json";

        this.mkdirIfNotExits(ruleDir);
        this.createFileIfNotExits(flowRulePath);
        this.createFileIfNotExits(degradeRulePath);
        this.createFileIfNotExits(systemRulePath);
        this.createFileIfNotExits(authorityRulePath);
        this.createFileIfNotExits(paramFlowRulePath);
        // 流控规则
        ReadableDataSource<String, List<FlowRule>> flowRuleRDS = new FileRefreshableDataSource<>
        (flowRulePath, flowRuleListParser
        );
        FlowRuleManager.register2Property(flowRuleRDS.getProperty());
        WritableDataSource<List<FlowRule>> flowRuleWDS = new FileWritableDataSource<>(flowRulePath, this::encodeJson);
        WritableDataSourceRegistry.registerFlowDataSource(flowRuleWDS);
        // 降级规则
        ReadableDataSource<String, List<DegradeRule>> degradeRuleRDS = new FileRefreshableDataSource<>(degradeRulePath, degradeRuleListParser);
        DegradeRuleManager.register2Property(degradeRuleRDS.getProperty());
        WritableDataSource<List<DegradeRule>> degradeRuleWDS = new FileWritableDataSource<>(degradeRulePath, this::encodeJson);
        WritableDataSourceRegistry.registerDegradeDataSource(degradeRuleWDS);
        // 系统规则
        ReadableDataSource<String, List<SystemRule>> systemRuleRDS = new FileRefreshableDataSource<>(systemRulePath, systemRuleListParser);
        SystemRuleManager.register2Property(systemRuleRDS.getProperty());
        WritableDataSource<List<SystemRule>> systemRuleWDS = new FileWritableDataSource<>(systemRulePath, this::encodeJson);
        WritableDataSourceRegistry.registerSystemDataSource(systemRuleWDS);
        // 授权规则
        ReadableDataSource<String, List<AuthorityRule>> authorityRuleRDS = new FileRefreshableDataSource<>(authorityRulePath, authorityRuleListParser);
        AuthorityRuleManager.register2Property(authorityRuleRDS.getProperty());
        WritableDataSource<List<AuthorityRule>> authorityRuleWDS = new FileWritableDataSource<>(authorityRulePath, this::encodeJson);
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityRuleWDS);
        // 热点参数规则
        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleRDS = new FileRefreshableDataSource<>( paramFlowRulePath,paramFlowRuleListParser);
        ParamFlowRuleManager.register2Property(paramFlowRuleRDS.getProperty());
        WritableDataSource<List<ParamFlowRule>> paramFlowRuleWDS = new FileWritableDataSource<>( paramFlowRulePath, this::encodeJson );
        ModifyParamFlowRulesCommandHandler.setWritableDataSource(paramFlowRuleWDS);
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject( source,  new TypeReference<List<FlowRule>>() { });

    private Converter<String, List<DegradeRule>> degradeRuleListParser = source -> JSON.parseObject(  source, new TypeReference<List<DegradeRule>>() {  });

    private Converter<String, List<SystemRule>> systemRuleListParser = source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {});

    private Converter<String, List<AuthorityRule>> authorityRuleListParser = source -> JSON.parseObject(source,new TypeReference<List<AuthorityRule>>() {} );

    private Converter<String, List<ParamFlowRule>> paramFlowRuleListParser = source -> JSON.parseObject(source,new TypeReference<List<ParamFlowRule>>() { } );

    private void mkdirIfNotExits(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void createFileIfNotExits(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    private <T> String encodeJson(T t) {
        return JSON.toJSONString(t);
    }
}

```

添加配置
 在resources下创建配置目录 META-INF/services ,然后添加文件`com.alibaba.csp.sentinel.init.InitFunc`

在文件中添加配置类的全路径

```
cn.maruifu.config.FilePersistence
```



## Feign整合Sentinel

第1步: 引入sentinel的依赖

```
 
<!--sentinel客户端-->
<dependency>
	<groupId>com.alibaba.cloud</groupId> 
	<artifactId>spring-cloud-starter-alibaba-sentinel</artifactId> 
</dependency>
```

第2步: 在配置文件中开启Feign对Sentinel的支持

```
feign:
	sentinel:
		enabled: true
```

第3步: 创建容错类

```
//容错类要求必须实现被容错的接口,并为每个方法实现容错方案
package cn.maruifu.service.api;

import cn.maruifu.vo.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//容错类要求必须实现被容错的接口,并为每个方法实现容错方案
@Component
@Slf4j
public class ProductApiServiceFallBack implements ProductApiService {
    @Override
    public Product findByPid(Integer pid) {
        Product product = new Product();
        product.setPid(-1);
        return product;
    }
}
```

第4步: 为被容器的接口指定容错类

```
package cn.maruifu.service.api;

import cn.maruifu.vo.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//value用于指定调用nacos下哪个微服务
//fallback用于指定容错类
@FeignClient(value = "service-product", fallback = ProductApiServiceFallBack.class)
//声明调用的提供者的name
public interface ProductApiService {
    //指定调用提供者的哪个方法
//@FeignClient+@GetMapping 就是一个完整的请求路径 http://service- product/product/{pid}
    @GetMapping(value = "/product/{pid}")
    Product findByPid(@PathVariable("pid") Integer pid);
}

```

第5步: 修改controller

```
package cn.maruifu.controller;

import cn.maruifu.service.OrderService;
import cn.maruifu.service.api.ProductApiService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

@RestController
@Slf4j
public class OrderController {
     @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrderService orderService;

    @Autowired
    private DiscoveryClient discoveryClient;




    @Autowired
    private ProductApiService productApiService;

    //准备买1件商品
    @GetMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info(">>客户下单，这时候要调用商品微服务查询商品信息");

        // 方法一 通过IP调用
        //通过restTemplate调用商品微服务
        //Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);

        // 方法二 从nacos中获取服务地址
        //ServiceInstance serviceInstance = discoveryClient.getInstances("service-product").get(0);
        //String url = serviceInstance.getHost() + ":" +serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        // 方法三 从nacos中获取服务地址 自定义规则实现随机挑选服务
        //List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        //int index = new Random().nextInt(instances.size());
        //ServiceInstance serviceInstance = instances.get(index);
        //String url = serviceInstance.getHost() + ":" + serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        // 方法四  基于Ribbon实现负载均衡
        //直接使用微服务名字， 从nacos中获取服务地址
        //String url = "service-product";
        //Product product = restTemplate.getForObject( "http://" + url + "/product/" + pid, Product.class);

        // 方法五 通过fegin调用商品微服务

        //  调用商品微服务,查询商品信息
        log.info("接收到{}号商品的下单请求,接下来调用商品微服务查询此商品信息", pid);

        Product product = productApiService.findByPid(pid);

        if (product.getPid() == -1) {
            Order order = new Order();
            order.setPname("下单失败");
            return order;
        }



        log.info("查询到{}号商品的信息,内容是:{}", pid, JSON.toJSONString(product)); //模拟一次网络延时
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //下单(创建订单)
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(product.getPid());
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);


        //为了不产生太多垃圾数据,暂时不做订单保存
        //orderService.save(order);
        log.info("创建订单成功,订单信息为{}", JSON.toJSONString(order));

        return order;
    }


    @RequestMapping("/order/message")
    public String message() {
        return "高并发下的问题测试";
    }
}

```

第6步: 停止所有 shop-product 服务,重启 shop-order 服务,访问请求,观察容错效果

![截屏2021-01-29 下午9.53.43](https://nas.mrf.ink:10001/images/2021/01/29/2021-01-29-9.53.43.png)



**扩展**: 如果想在容错类中拿到具体的错误,可以使用下面的方式

```

package cn.maruifu.service.api;

import cn.maruifu.vo.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//value用于指定调用nacos下哪个微服务
//fallback用于指定容错类
//@FeignClient(value = "service-product", fallback = ProductApiServiceFallBack.class)

@FeignClient(   value = "service-product", fallbackFactory = ProductApiServiceFallBackFactory.class)
//声明调用的提供者的name
public interface ProductApiService {
    //指定调用提供者的哪个方法
//@FeignClient+@GetMapping 就是一个完整的请求路径 http://service- product/product/{pid}
    @GetMapping(value = "/product/{pid}")
    Product findByPid(@PathVariable("pid") Integer pid);
}




package cn.maruifu.service.api;
import cn.maruifu.vo.Product;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
@Component
public class ProductApiServiceFallBackFactory implements FallbackFactory<ProductApiService> {
    @Override
    public ProductApiService create(Throwable throwable) {
        return new ProductApiService() {
            @Override
            public Product findByPid(Integer pid) {
                throwable.printStackTrace();
                Product product = new Product();
                product.setPid(-1);
                return product;
            }
        };
    }
}


  
```

**注意: fallback和fallbackFactory只能使用其中一种方式**
## 服务治理介绍

**先来思考一个问题**？

通过上一章的操作，我们已经可以实现微服务之间的调用。但是我们把服务提供者的网络地址(ip，端 口)等硬编码到了代码中，这种做法存在许多问题:

一旦服务提供者地址变化，就需要手工修改代码 一旦是多个服务提供者，无法实现负载均衡功能 一旦服务变得越来越多，人工维护调用关系困难

那么应该怎么解决呢， 这时候就需要通过注册中心动态的实现**服务治理**。

###  **什么是服务治理** 

服务治理是微服务架构中最核心最基本的模块。用于实现各个微服务的**自动化注册与发现**。

**服务注册:**在服务治理框架中，都会构建一个注册中心，每个服务单元向注册中心登记自己提供服 务的详细信息。并在注册中心形成一张服务的清单，服务注册中心需要以心跳的方式去监测清单中 的服务是否可用，如果不可用，需要在服务清单中剔除不可用的服务。 

**服务发现:**服务调用方向服务注册中心咨询服务，并获取所有服务的实例清单，实现对具体服务实例的访问。

![服务治理](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127213529134.png)

通过上面的调用图会发现，除了微服务，还有一个组件是**服务注册中心**，它是微服务架构非常重要的一 个组件，在微服务架构里主要起到了协调者的一个作用。注册中心一般包含如下几个功能:

1. 服务发现: 服务注册:保存服务提供者和服务调用者的信息

   服务订阅:服务调用者订阅服务提供者的信息，注册中心向订阅者推送提供者的信息

2. 服务配置:

   - 配置订阅:服务提供者和服务调用者订阅微服务相关的配置
   - 配置下发:主动将配置推送给服务提供者和服务调用者

3. 服务健康检测 检测服务提供者的健康情况，如果发现异常，执行服务剔除

### **常见的注册中心**

- **Zookeeper**

zookeeper是一个分布式服务框架，是Apache Hadoop 的一个子项目，它主要是用来解决分布式 应用中经常遇到的一些数据管理问题，如:统一命名服务、状态同步服务、集群管理、分布式应用 配置项的管理等。

-  **Eureka**

Eureka是Springcloud Netflix中的重要组件，主要作用就是做服务注册和发现。但是现在已经闭 源

-  **Consul** 

Consul是基于GO语言开发的开源工具，主要面向分布式，服务化的系统提供服务注册、服务发现 和配置管理的功能。Consul的功能都很实用，其中包括:服务注册/发现、健康检查、Key/Value 存储、多数据中心和分布式一致性保证等特性。Consul本身只是一个二进制的可执行文件，所以 安装和部署都非常简单，只需要从官网下载后，在执行对应的启动脚本即可。

- **Nacos**

Nacos是一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。他是SpringCloud Alibaba 组件之一，负责服务注册发现和服务配置，可以这样认为 nacos=eureka+config。



## nacos简介

Nacos 致力于帮助您发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，帮助您快速实现

动态服务发现、服务配置、服务元数据及流量管理。 从上面的介绍就可以看出，**nacos的作用就是一个注册中心**，用来管理注册上来的各个微服务。

## nacos实战入门

接下来，我们就在现有的环境中加入nacos，并将我们的两个微服务注册上去。

### 搭建nacos环境

#### 安装nacos

从 Github 上下载源码方式

```bash
git clone https://github.com/alibaba/nacos.git
cd nacos/
mvn -Prelease-nacos -Dmaven.test.skip=true clean install -U  
ls -al distribution/target/

// change the $version to your actual path
cd distribution/target/nacos-server-$version/nacos/bin
```

下载编译后压缩包方式

您可以从 [最新稳定版本](https://github.com/alibaba/nacos/releases) 下载 `nacos-server-$version.zip` 包。

```bash
  unzip nacos-server-$version.zip 或者 tar -xvf nacos-server-$version.tar.gz
  cd nacos/bin
```

#### 启动nacos

Linux/Unix/Mac启动命令(standalone代表着单机模式运行，非集群模式):

```
sh startup.sh -m standalone
```

如果您使用的是ubuntu系统，或者运行脚本报错提示[[符号找不到，可尝试如下运行：

```
bash startup.sh -m standalone
```

Windows启动命令(standalone代表着单机模式运行，非集群模式):

```
cmd startup.cmd -m standalone
```

>启动失败看一下自己是不是64位 1.8+JDK



#### 访问nacos

 打开浏览器输入http://localhost:8848/nacos，即可访问服务， 默认密码是nacos/nacos

![ 访问nacos](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127214154181.png)

### 将商品微服务注册到nacos 

接下来开始修改 shop-product 模块的代码， 将其注册到nacos服务上

在pom.xml中添加nacos的依赖

```
 
<!--nacos客户端-->
<dependency>
	<groupId>com.alibaba.cloud</groupId> 
	<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId> 
</dependency>
```

在主类上添加**@EnableDiscoveryClient**注解

```
@SpringBootApplication
@EnableDiscoveryClient
public class ProductApplication
```

在application.yml中添加nacos服务的地址

```
spring:
    cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

启动服务， 观察nacos的控制面板中是否有注册上来的商品微服务

![商品微服务](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127214557772.png)

### 将订单微服务注册到nacos

接下来开始修改 shop_order 模块的代码， 将其注册到nacos服务上

在pom.xml中添加nacos的依赖

```
 
<!--nacos客户端-->
<dependency>
	<groupId>com.alibaba.cloud</groupId> 
	<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId> 
</dependency>
```

在主类上添加**@EnableDiscoveryClient**注解

```
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication
```

在application.yml中添加nacos服务的地址

```
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

修改OrderController， 实现微服务调用

```
package cn.maruifu.controller;

import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class OrderController {
     @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrderService orderService;

    @Autowired
    private DiscoveryClient discoveryClient;


    //准备买1件商品
    @GetMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info(">>客户下单，这时候要调用商品微服务查询商品信息");

        // 之前调用
        //通过restTemplate调用商品微服务
        //Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);

        //从nacos中获取服务地址
        ServiceInstance serviceInstance = discoveryClient.getInstances("service-product").get(0);
        String url = serviceInstance.getHost() + ":" +serviceInstance.getPort();
        log.info(">>从nacos中获取到的微服务地址为:" + url);
        Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);
        
        log.info(">>商品信息,查询结果:"+	JSON.toJSONString(product));
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(product.getPid());
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        orderService.save(order);
        return order;
    }
}
 
```

> DiscoveryClient是专门负责服务注册和发现的，我们可以通过它获取到注册到注册中心的所有服务



启动服务， 观察nacos的控制面板中是否有注册上来的订单微服务，然后通过访问消费者服务验证调 用是否成功

![订单微服务](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127214959915.png)

## 实现服务调用的负载均衡

### 什么是负载均衡

通俗的讲， 负载均衡就是将负载(工作任务，访问请求)进行分摊到多个操作单元(服务器,组件)上 进行执行。

根据负载均衡发生位置的不同,一般分为**服务端负载均衡**和**客户端负载均衡**。 服务端负载均衡指的是发生在服务提供者一方,比如常见的nginx负载均衡

而客户端负载均衡指的是发生在服务请求的一方，也就是在发送请求之前已经选好了由哪个实例处理请求。

![负载均衡](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127215055557.png)

我们在微服务调用关系中一般会选择客户端负载均衡，也就是在服务调用的一方来决定服务由哪个提供 者执行。

### 自定义实现负载均衡 

1通过idea再启动一个 shop-product 微服务，点击Copy Configurations 设置其端口为8082

![image-20210127215133849](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127215133849.png)

2通过nacos查看微服务的启动情况

![image-20210127215154924](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127215154924.png)

3修改 shop-order 的代码，实现负载均衡

```
package cn.maruifu.controller;

import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


    //准备买1件商品
    @GetMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info(">>客户下单，这时候要调用商品微服务查询商品信息");

        // 之前调用
        //通过restTemplate调用商品微服务
        //Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);

        //从nacos中获取服务地址
        //ServiceInstance serviceInstance = discoveryClient.getInstances("service-product").get(0);
        //String url = serviceInstance.getHost() + ":" +serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        //从nacos中获取服务地址 自定义规则实现随机挑选服务
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        int index = new Random().nextInt(instances.size());
        ServiceInstance serviceInstance = instances.get(index);
        String url = serviceInstance.getHost() + ":" + serviceInstance.getPort();
        log.info(">>从nacos中获取到的微服务地址为:" + url);
        Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);


        log.info(">>商品信息,查询结果:"+	JSON.toJSONString(product));
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(product.getPid());
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        orderService.save(order);
        return order;
    }
}
```

4启动两个服务提供者和一个服务消费者，多访问几次消费者测试效果

![image-20210127215418236](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127215418236.png)

### 基于Ribbon实现负载均衡

**Ribbon** 是 Spring Cloud 的一个组件， 它可以让我们使用一个注解就能轻松的搞定负载均衡 

第1步:在RestTemplate 的生成方法上添加@LoadBalanced注解

```
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
		return new RestTemplate();
}
```

第2步:修改服务调用的方法

```
package cn.maruifu.controller;

import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


    //准备买1件商品
    @GetMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info(">>客户下单，这时候要调用商品微服务查询商品信息");

        // 之前调用
        //通过restTemplate调用商品微服务
        //Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);

        //从nacos中获取服务地址
        //ServiceInstance serviceInstance = discoveryClient.getInstances("service-product").get(0);
        //String url = serviceInstance.getHost() + ":" +serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        //从nacos中获取服务地址 自定义规则实现随机挑选服务
        //List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        //int index = new Random().nextInt(instances.size());
        //ServiceInstance serviceInstance = instances.get(index);
        //String url = serviceInstance.getHost() + ":" + serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        // 基于Ribbon实现负载均衡
        //直接使用微服务名字， 从nacos中获取服务地址
        String url = "service-product";
        Product product = restTemplate.getForObject( "http://" + url + "/product/" + pid, Product.class);

        log.info(">>商品信息,查询结果:"+	JSON.toJSONString(product));
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(product.getPid());
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        orderService.save(order);
        return order;
    }
}
```

**Ribbon支持的负载均衡策略** 

Ribbon内置了多种负载均衡策略,内部负载均衡的顶级接口为com.netflix.loadbalancer.IRule , 

具体的负载策略如下图所示:

![负载策略](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127215929118.png)

我们可以通过修改配置来调整Ribbon的负载均衡策略，具体代码如下

```
service-product: # 调用的提供者的名称
	ribbon:
		NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule

```



## 基于Feign实现服务调用 

### 什么是Feign 

Feign是Spring Cloud提供的一个声明式的伪Http客户端， 它使得调用远程服务就像调用本地服务一样 简单， 只需要创建一个接口并添加一个注解即可。

Nacos很好的兼容了Feign， Feign默认集成了 Ribbon， 所以在Nacos下使用Fegin默认就实现了负载均 衡的效果。

### Feign的使用

#### 加入Fegin的依赖

```
<!--fegin组件-->
<dependency> 
	<groupId>org.springframework.cloud</groupId> 
	<artifactId>spring-cloud-starter-openfeign</artifactId> 
</dependency>
```

#### 在主类上添加Fegin的注解

```
@SpringBootApplication 
@EnableDiscoveryClient 
@EnableFeignClients
//开启Fegin 
public class OrderApplication {}
```

#### 创建一个service

```
package cn.maruifu.api;

import cn.maruifu.vo.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-product")
//声明调用的提供者的name
public interface ProductApiService {
    //指定调用提供者的哪个方法
    //@FeignClient+@GetMapping 就是一个完整的请求路径 http://service- product/product/{pid}
    @GetMapping(value = "/product/{pid}")
    Product findByPid(@PathVariable("pid") Integer pid);
}
```

#### 修改controller代码

```
package cn.maruifu.controller;

import cn.maruifu.api.ProductApiService;
import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

        // 之前调用
        //通过restTemplate调用商品微服务
        //Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);

        //从nacos中获取服务地址
        //ServiceInstance serviceInstance = discoveryClient.getInstances("service-product").get(0);
        //String url = serviceInstance.getHost() + ":" +serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        //从nacos中获取服务地址 自定义规则实现随机挑选服务
        //List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        //int index = new Random().nextInt(instances.size());
        //ServiceInstance serviceInstance = instances.get(index);
        //String url = serviceInstance.getHost() + ":" + serviceInstance.getPort();
        //log.info(">>从nacos中获取到的微服务地址为:" + url);
        //Product product = restTemplate.getForObject("http://" + url + "/product/" + pid, Product.class);

        // 基于Ribbon实现负载均衡
        //直接使用微服务名字， 从nacos中获取服务地址
        // String url = "service-product";
        // Product product = restTemplate.getForObject( "http://" + url + "/product/" + pid, Product.class);

        //通过fegin调用商品微服务
        Product product = productApiService.findByPid(pid);


        log.info(">>商品信息,查询结果:"+	JSON.toJSONString(product));
        Order order = new Order();
        order.setUid(1);
        order.setUsername("测试用户");
        order.setPid(product.getPid());
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        orderService.save(order);
        return order;
    }
}
 
```

#### 重启order微服务,查看效果
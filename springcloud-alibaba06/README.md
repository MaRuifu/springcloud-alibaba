## 服务配置中心介绍

首先我们来看一下,微服务架构下关于配置文件的一些问题:

1. 配置文件相对分散。在一个微服务架构下，配置文件会随着微服务的增多变的越来越多，而且分散 在各个微服务中，不好统一配置和管理。
2. 配置文件无法区分环境。微服务项目可能会有多个环境，例如:测试环境、预发布环境、生产环 境。每一个环境所使用的配置理论上都是不同的，一旦需要修改，就需要我们去各个微服务下手动 维护，这比较困难。
3. 配置文件无法实时更新。我们修改了配置文件之后，必须重新启动微服务才能使配置生效，这对一 个正在运行的项目来说是非常不友好的。

基于上面这些问题，我们就需要**配置中心**的加入来解决这些问题。

**配置中心的思路是:**

- 首先把项目中各种配置全部都放到一个集中的地方进行统一管理，并提供一套标准的接口。

-  当各个服务需要获取配置的时候，就来配置中心的接口拉取自己的配置。

-  当配置中心中的各种参数有更新的时候，也能通知到各个服务实时的过来同步最新的信息，使之动态更新。

**当加入了服务配置中心之后，我们的系统架构图会变成下面这样**

![截屏2021-08-29 下午4.23.36](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.23.36.png)

在业界常见的服务配置中心，有下面这些:

- **Apollo**

Apollo是由携程开源的分布式配置中心。特点有很多，比如:配置更新之后可以实时生效，支持灰度发 布功能，并且能对所有的配置进行版本管理、操作审计等功能，提供开放平台API。并且资料也写的很 详细。

- **Disconf**

Disconf是由百度开源的分布式配置中心。它是基于Zookeeper来实现配置变更后实时通知和生效的。

- **SpringCloud Confifig**

这是Spring Cloud中带的配置中心组件。它和Spring是无缝集成，使用起来非常方便，并且它的配置存 储支持Git。不过它没有可视化的操作界面，配置的生效也不是实时的，需要重启或去刷新。

- **Nacos**

这是SpingCloud alibaba技术栈中的一个组件，前面我们已经使用它做过服务注册中心。其实它也集成 了服务配置的功能，我们可以直接使用它作为服务配置中心。

## Nacos Config 入门

使用nacos作为配置中心，其实就是将nacos当做一个服务端，将各个微服务看成是客户端，我们将各个微服务的配置文件统一存放在nacos上，然后各个微服务从nacos上拉取配置即可。

接下来我们以商品微服务为例，学习nacos confifig的使用。

1. 搭建nacos环境【使用现有的nacos环境即可】
2. 在微服务中引入nacos的依赖

```
<dependency>
  <groupId>com.alibaba.cloud</groupId> 
  <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId> 
</dependency>
```

3. 在微服务中添加nacos confifig的配置

**注意:不能使用原来的application.yml作为配置文件，而是新建一个bootstrap.yml作为配置文件**

配置文件优先级(由高到低):

bootstrap.properties -> bootstrap.yml -> application.properties -> application.yml

```
spring:
  application:
    name: service-product
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848 #nacos中心地址
        file-extension: yaml # 配置文件格式 profiles:
  profiles:
    active: dev # 环境标识
```

4. 在nacos中添加配置

> 点击配置列表，点击右边+号，新建配置。在新建配置过程中，要注意下面的细节:
>
> 1)Data ID不能随便写，要跟配置文件中的对应，对应关系如图所示
>
> 2)配置文件格式要跟配置文件的格式对应，且目前仅仅支持YAML和Properties
>
> 3)配置内容按照上面选定的格式书写

![截屏2021-08-29 下午4.30.26](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.30.26.png)



5. 注释本地的application.yam中的内容， 启动程序进行测试

如果依旧可以成功访问程序，说明我们nacos的配置中心功能已经实现

## Nacos Config 深入

### **配置动态刷新**

在入门案例中，我们实现了配置的远程存放，但是此时如果修改了配置，我们的程序是无法读取到的， 因此，我们需要开启配置的动态刷新功能。

在nacos中的service-product-dev.yaml配置项中添加下面配置:

```
config:
  appName: product
```

方式一:硬编码方式

```
@RestController
public class NacosConfigController {
@Autowired
private ConfigurableApplicationContext applicationContext; 

	  @GetMapping( "/nacos-config-test1" )
    public String nacosConfingTest1(){
    	return (applicationContext.getEnvironment().getProperty("config.appName"));
    }
}
```

方式二:注解方式(推荐)

```
@RestController
@RefreshScope /* 只需要在需要动态读取配置的类上添加此注解就可以 */
public class NacosConfigController { 

@Value("${config.appName}")
private String appName; 

    @GetMapping("/nacos-config-test2")
    public String nacosConfingTest2(){
        return(appName);
    }
}
```

### **配置共享**

当配置越来越多的时候，我们就发现有很多配置是重复的，这时候就考虑可不可以将公共配置文件提取出来，然后实现共享呢?当然是可以的。接下来我们就来探讨如何实现这一功能。

**同一个微服务的不同环境之间共享配置**

如果想在同一个微服务的不同环境之间实现配置共享，其实很简单。只需要提取一个以 spring.application.name 命名的配置文件，然后将其所有环境的公共配置放在里面即可。

1. 新建一个名为service-product.yaml配置存放商品微服务的公共配置

![截屏2021-08-29 下午4.35.11](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.35.11.png)

2. 新建一个名为service-product-test.yaml配置存放测试环境的配置

![截屏2021-08-29 下午4.35.36](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.35.36.png)

3. 新建一个名为service-productr-dev.yaml配置存放开发环境的配置

![截屏2021-08-29 下午4.36.33](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.36.33.png)

4. 添加测试方法
```
@RestController
@RefreshScope
public class NacosConfigController { 

@Value( "${config.env}" )
private String env;

/* 3 同一微服务的不同环境下共享配置 */
@GetMapping( "/nacos-config-test3" )
    public String nacosConfingTest3(){
        return(env);
    }
}
```

5. 访问测试

![截屏2021-08-29 下午4.38.26](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.38.26.png)

6. 接下来，修改bootstrap.yml中的配置，将active设置成test，再次访问，观察结果

```
spring:
  profiles:
    active: test # 环境标识
```

**不同微服务中间共享配置**

不同为服务之间实现配置共享的原理类似于文件引入，就是定义一个公共配置，然后在当前配置中引 入。

1. 在nacos中定义一个DataID为all-service.yaml的配置，用于所有微服务共享

```
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://shop?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true 
    username: root
    password: root
jpa:
  properties:
    hibernate:
      hbm2ddl:
        auto: update
		dialect: org.hibernate.dialect.MySQL5InnoDBDialect
  cloud:
    nacos:
      discovery:
      	server-addr: 127.0.0.1:8848
```

2. 在nacos的中修改service-product.yaml中为下面内容

```
server:
  port: 8081
config:
  appName: product
```

3. 修改bootstrap.yaml

```
spring:
  application:
  	name: service-product
  profiles:
    active: dev # 环境标识
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848 #nacos中心地址
        file-extension: yaml # 配置文件格式
        shared-dataids: all-service.yaml # 配置要引入的配置 
        refreshable-dataids: all-service.yaml # 配置要实现动态配置刷新的配置 
```

4. 启动商品微服务进行测试




## Nacos 的几个概念

**命名空间(Namespace)** 命名空间可用于进行不同环境的配置隔离。一般一个环境划分到一个命名空间

**配置分组(Group)** 配置分组用于将不同的服务可以归类到同一分组。一般将一个项目的配置分到一组

**配置集(Data ID)** 在系统中，一个配置文件通常就是一个配置集。一般微服务的配置就是一个配置集

![截屏2021-08-29 下午4.46.16](https://nas.mrf.ink:10001/images/2021/08/29/2021-08-29-4.46.16.png)





结果如下

![image-20220223160522368](https://nas.mrf.ink:10001/images/2022/02/23/image-20220223160522368.png)

```
# all-service.yaml


spring:
  zipkin:
    #开启zipkin分析
    enabled: true
    #让nacos把它当成一个URL，而不要当做服务名
    discoveryClientEnabled: false
  sleuth:
    sampler:
      #限速器，每秒采集10个请求，防止大并发过载。推荐
      #rate: 10
      #采集率，大并发可能采集率数量也会很高。采样的百分比
      probability: 0.1
  application:
    name: service-product
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    show-sql: true
    hibernate:
      ddl-auto: update
      use-new-id-generator-mappings: false

```



```
# service-product.yaml

config:
  appName: product
```



```
# service-product-dev.yaml

config:
  env: dev

server:
  port: 8081
spring:
  zipkin:
    #zipkin服务地址
    baseUrl: http://127.0.0.1:9411/
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/spring-cloud?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: root
    password: Mrf12345
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```



```
# service-product-test.yaml

config:
  env: test

server:
  port: 8081
spring:
  zipkin:
    #zipkin服务地址
    baseUrl: http://127.0.0.1:9411/
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/spring-cloud?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: root
    password: Mrf12345
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```


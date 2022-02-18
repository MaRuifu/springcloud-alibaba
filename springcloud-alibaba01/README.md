我们本次是使用的电商项目中的商品、订单、用户为案例进行讲解.

## 案例准备

### 技术选型

**maven:** 3.3.9

**数据库:** MySQL 5.7

**持久层:** SpingData Jpa

**其他:** SpringCloud Alibaba 技术栈

### 模块设计

springcloud-alibaba 父工程

shop-common 公共模块【实体类】 

 shop-user 用户微服务 【端口: 8071】 

 shop-product 商品微服务 【端口: 8081】 

 shop-order 订单微服务 【端口: 8091】

![模块设计](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127203623839.png)

### 微服务调用 

在微服务架构中，最常见的场景就是微服务之间的相互调用。我们以电商系统中常见的**用户下单**为例来 演示微服务的调用:客户向订单微服务发起一个下单的请求，在进行保存订单之前需要调用商品微服务 查询商品的信息。

我们一般把服务的主动调用方称为**服务消费者**，把服务的被调用方称为**服务提供者**。

![微服务调用](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127203651412.png)

在这种场景下，订单微服务就是一个服务消费者， 商品微服务就是一个服务提供者。

## 创建数据库

```sql
DROP DATABASE IF EXISTS `shop`;
CREATE DATABASE  `shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `shop`;
CREATE TABLE `shop_order` (
`oid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
`username` varchar(255) DEFAULT NULL COMMENT '用户名',
`uid` int DEFAULT NULL COMMENT '用户id',
`pid` int DEFAULT NULL COMMENT '商品ID',
`pname` varchar(255) DEFAULT NULL COMMENT '商品名称',
`pprice` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
`number` int DEFAULT NULL COMMENT '数量',
PRIMARY KEY (`oid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `shop_product` (
`pid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
`pname` varchar(255) DEFAULT NULL COMMENT '商品名称',
`pprice` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
`stock` int DEFAULT NULL COMMENT '库存',
PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `shop_user` (
`uid` int NOT NULL AUTO_INCREMENT COMMENT '主键',
`username` varchar(255) DEFAULT NULL COMMENT '用户名',
`password` varchar(255) DEFAULT NULL COMMENT '密码',
`telephone` varchar(255) DEFAULT NULL COMMENT '手机号',
PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```





## 创建父工程

创建一个maven工程，然后在pom.xml文件中添加下面内容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.3.RELEASE</version>
    </parent>
    <groupId>cn.maruifu</groupId>
    <artifactId>springcloud-alibaba</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF- 8</project.reporting.outputEncoding>
        <spring-cloud.version>Greenwich.RELEASE</spring-cloud.version>
        <spring-cloud-alibaba.version>2.1.0.RELEASE</spring-cloud-alibaba.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>

```

版本对应：

![版本对应](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127205447364.png)

## 创建基础模块 

创建 shop-common 模块:

在pom.xml中添加依赖

```
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>springcloud-alibaba</artifactId>
        <groupId>cn.maruifu</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>shop-common</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.56</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.6</version>
        </dependency>
    </dependencies>
</project>

```

创建实体类

```Java
//用户
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
@Entity(name = "shop_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid;//主键

    private String username;//用户名

    private String password;//密码

    private String telephone;//手机号

}

//商品
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
@Entity(name = "shop_product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pid;//主键

    private String pname;//商品名称

    private Double pprice;//商品价格

    private Integer stock;//库存

}

//订单
package cn.maruifu.vo;

import lombok.Data;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

//订单
@Entity(name = "shop_order")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oid;//订单id

    private Integer uid;//用户id

    private String username;//用户名

    private Integer pid;//商品ID


    private String pname;//商品名称

    private Double pprice;//商品价格

    private Integer number;//数量

}
```





## 创建用户微服务

新建一个 shop-user 模块，然后进行下面操作

###  创建pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <parent>
        <artifactId>springcloud-alibaba</artifactId>
        <groupId>cn.maruifu</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>shop-user</artifactId>
    <dependencies>
        <dependency>
            <groupId>cn.maruifu</groupId>
            <artifactId>shop-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 编写主类

```
package cn.maruifu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
```

### 创建配置文件

```
server:
  port: 8071
spring:
  application:
    name: service-user
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/spring-cloud?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: root
    password: Mrf12345
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    show-sql: true
    hibernate:
      ddl-auto: update
      use-new-id-generator-mappings: false

```



## 创建商品微服务

###  创建pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>springcloud-alibaba</artifactId>
        <groupId>cn.maruifu</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>shop-product</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.maruifu</groupId>
            <artifactId>shop-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>

```

### 编写主类

```
@SpringBootApplication

public class ProductApplication {
    public static void main(String[] args) {
       SpringApplication.run(ProductApplication.class, args);
    }

}
```

###  创建配置文件

```
server:
  port: 8081
spring:
  application:
    name: service-product
  datasource:
    url: jdbc:mysql://shop?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    show-sql: true
    hibernate:
      ddl-auto: update
      use-new-id-generator-mappings: false


```

### 创建ProductDao接口

```java
package cn.maruifu.dao;

import cn.maruifu.vo.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDao extends JpaRepository<Product,Integer>  {
}
```

### 创建ProductService类

```java
package cn.maruifu.service.impl;

import cn.maruifu.dao.ProductDao;
import cn.maruifu.service.ProductService;
import cn.maruifu.vo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;


    @Override
    public Product findByPid(Integer pid) {
        return productDao.findById(pid).get();
    }

}
```

### 创建Controller

```java
package cn.maruifu.controller;

import cn.maruifu.service.ProductService;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;
    @GetMapping("/product/{pid}")
    public Product product(@PathVariable("pid") Integer pid) {
        Product product = productService.findByPid(pid);
        log.info("查询到商品:" + JSON.toJSONString(product));
        return product;
    }

}
```

### 启动工程

等到数据库表创建完毕之后，加入测试数据

```sql
INSERT INTO shop_product VALUE(NULL,'小米','1000','5000'); 

INSERT INTO shop_product VALUE(NULL,'华为','2000','5000'); 

INSERT INTO shop_product VALUE(NULL,'苹果','3000','5000'); 

INSERT INTO shop_product VALUE(NULL,'OPPO','4000','5000');
```

### 通过浏览器访问服务

![image-20210127211736478](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127211736478.png)

## 创建订单微服务

### 创建pom.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>springcloud-alibaba</artifactId>
        <groupId>cn.maruifu</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>shop-order</artifactId>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.maruifu</groupId>
            <artifactId>shop-common</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>

```

### 编写主类

```
package cn.maruifu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) { 
        SpringApplication.run(OrderApplication.class, args); 
    }
}
```

### 创建配置文件

```
server:
  port: 8091
spring:
  application:
    name: service-order
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://shop?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
    username: root
    password: root
   
  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
    show-sql: true
    hibernate:
      ddl-auto: update
      use-new-id-generator-mappings: false

```

### 创建OrderDao接口

```java
package cn.maruifu.dao;

import cn.maruifu.vo.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDao extends JpaRepository<Order,Long> {

}
```

### 创建OrderService类

```java
package cn.maruifu.service.impl;

import cn.maruifu.dao.OrderDao;
import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Override
    public void save(Order order) {
        orderDao.save(order);
    }
}
```

### 创建RestTemplate

```java
package cn.maruifu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }


    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}

```

### 创建Controller

```java
package cn.maruifu.controller;

import cn.maruifu.service.OrderService;
import cn.maruifu.vo.Order;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    //准备买1件商品
    @GetMapping("/order/prod/{pid}")
    public Order order(@PathVariable("pid") Integer pid) {
        log.info(">>客户下单，这时候要调用商品微服务查询商品信息");
        //通过restTemplate调用商品微服务
        Product product = restTemplate.getForObject("http://localhost:8081/product/" + pid, Product.class);
        log.info(">>商品信息,查询结果:" + JSON.toJSONString(product));
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

### 浏览器访问服务进行测试

![image-20210127212306481](https://nas.mrf.ink:10001/images/2021/01/27/image-20210127212306481.png)



[源码地址](https://github.com/MaRuifu/springcloud-alibaba/tree/main/springcloud-alibaba)
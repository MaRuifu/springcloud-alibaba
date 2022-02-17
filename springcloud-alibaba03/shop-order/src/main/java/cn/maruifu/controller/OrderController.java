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

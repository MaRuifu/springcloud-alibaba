package cn.maruifu.service.api;

import cn.maruifu.vo.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//value用于指定调用nacos下哪个微服务
//fallback用于指定容错类
@FeignClient(value = "service-product", fallback = ProductApiServiceFallBack.class)

//@FeignClient(   value = "service-product", fallbackFactory = ProductApiServiceFallBackFactory.class)
//声明调用的提供者的name
public interface ProductApiService {
    //指定调用提供者的哪个方法
//@FeignClient+@GetMapping 就是一个完整的请求路径 http://service- product/product/{pid}
    @GetMapping(value = "/product/{pid}")
    Product findByPid(@PathVariable("pid") Integer pid);
}

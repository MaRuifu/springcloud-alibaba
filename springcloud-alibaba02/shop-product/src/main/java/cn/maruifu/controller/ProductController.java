package cn.maruifu.controller;

import cn.maruifu.service.ProductService;
import cn.maruifu.vo.Product;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
 import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@Slf4j
public class ProductController {

    @Resource
    private ProductService productService;
    @GetMapping("/product/{pid}")
    public Product product(@PathVariable("pid") Integer pid) {
        Product product = productService.findByPid(pid);
        log.info("查询到商品:" + JSON.toJSONString(product));
        return product;
    }

}
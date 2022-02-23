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

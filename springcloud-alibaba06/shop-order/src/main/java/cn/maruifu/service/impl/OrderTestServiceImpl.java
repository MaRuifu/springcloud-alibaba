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
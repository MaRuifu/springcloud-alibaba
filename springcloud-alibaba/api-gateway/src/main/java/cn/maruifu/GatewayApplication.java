package cn.maruifu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//master
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
    //ceshi
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
package cn.maruifu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope /* 只需要在需要动态读取配置的类上添加此注解就可以 */
public class NacosConfigController {

    @Value("${config.appName}")
    private String appName;


    @Value( "${config.env}" )
    private String env;

   // 硬编码方式
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @GetMapping( "/nacos-config-test1" )
    public String nacosConfingTest1(){
        return (applicationContext.getEnvironment().getProperty("config.appName"));
    }


    //注解方式(推荐)
    @GetMapping("/nacos-config-test2")
    public String nacosConfingTest2(){
        return(appName);
    }




    /* 3 同一微服务的不同环境下共享配置 */
    @GetMapping( "/nacos-config-test3" )
    public String nacosConfingTest3(){
        return(env);
    }

}
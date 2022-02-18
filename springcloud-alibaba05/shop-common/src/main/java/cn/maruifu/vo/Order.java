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
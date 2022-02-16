package cn.maruifu.dao;

import cn.maruifu.vo.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDao extends JpaRepository<Order,Long> {

}
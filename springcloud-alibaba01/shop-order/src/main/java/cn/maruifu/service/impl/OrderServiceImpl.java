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
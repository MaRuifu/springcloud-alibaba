package cn.maruifu.service.impl;

import cn.maruifu.dao.ProductDao;
import cn.maruifu.service.ProductService;
import cn.maruifu.vo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;


    @Override
    public Product findByPid(Integer pid) {
        return productDao.findById(pid).get();
    }

}
package cn.maruifu.service;

import cn.maruifu.vo.Product;
import org.springframework.stereotype.Service;

public interface ProductService {
    Product findByPid(Integer pid);
}

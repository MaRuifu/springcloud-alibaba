package cn.maruifu.dao;

import cn.maruifu.vo.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDao extends JpaRepository<Product,Integer>  {
}
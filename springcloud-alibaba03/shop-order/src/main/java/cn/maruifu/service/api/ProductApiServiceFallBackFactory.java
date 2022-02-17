package cn.maruifu.service.api;


import cn.maruifu.vo.Product;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductApiServiceFallBackFactory implements FallbackFactory<ProductApiService> {
    @Override
    public ProductApiService create(Throwable throwable) {
        return new ProductApiService() {
            @Override
            public Product findByPid(Integer pid) {
                throwable.printStackTrace();
                Product product = new Product();
                product.setPid(-1);
                return product;
            }
        };
    }
}

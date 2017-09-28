package com.rjxx.taxeasy.wechat.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Administrator on 2017-05-27.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Order {

    private OrderMain OrderMain;

    @XmlElementWrapper(name = "OrderDetails")
    private List<ProductItem> ProductItem;

    public OrderMain getOrderMain() {
        return OrderMain;
    }

    public void setOrderMain(OrderMain orderMain) {
        OrderMain = orderMain;
    }

    public List<ProductItem> getProductItem() {
        return ProductItem;
    }

    public void setProductItem(List<ProductItem> productItem) {
        ProductItem = productItem;
    }
}

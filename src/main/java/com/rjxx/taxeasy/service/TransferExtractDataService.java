package com.rjxx.taxeasy.service;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/3/30
 */

@Component
public class TransferExtractDataService {
    private static Logger logger = LoggerFactory.getLogger(TransferExtractDataService.class);
    public AdapterPost seaway(String tq) {
        AdapterPost post = new AdapterPost();
        return post;
    }

    public AdapterPost kgc(String tq) {
        AdapterPost post = new AdapterPost();
        return post;
    }

    public AdapterPost fujifilm(String tq) {
        AdapterPost post = new AdapterPost();
        return post;
    }

    public AdapterPost test(String tq) {
        logger.info("抽取数据KEY={}",tq);
        AdapterPost post = new AdapterPost();
        AdapterData data = new AdapterData();
        AdapterDataOrder order = new AdapterDataOrder();
        AdapterDataSeller seller = new AdapterDataSeller();
        List<AdapterDataOrderDetails> details = new ArrayList<>();
        List<AdapterDataOrderPayments> payments = new ArrayList<>();

        //明细
        for (int i = 2; i > 0; i--) {
            AdapterDataOrderDetails detail = new AdapterDataOrderDetails();
            detail.setAmount(5d);
            detail.setMxTotalAmount(5d);
            detail.setPolicyMark("0");
            detail.setProductCode("3070401000000000000");
            detail.setProductName("餐饮服务");
            detail.setQuantity(1d);
            detail.setUnitPrice(5d);
            detail.setUtil("次");
            detail.setRowType("0");
            detail.setTaxRate(0.06);
            details.add(detail);
        }

        //支付
        AdapterDataOrderPayments payment = new AdapterDataOrderPayments();
        payment.setPayCode("02");
        payment.setPayPrice(5d);
        payments.add(payment);

        AdapterDataOrderPayments payment2 = new AdapterDataOrderPayments();
        payment2.setPayCode("04");
        payment2.setPayPrice(5d);
        payments.add(payment2);

        //订单
        order.setPayments(payments);
        order.setOrderDetails(details);
        order.setOrderNo(System.currentTimeMillis() + "");
        order.setOrderDate(new Date());
        order.setTotalAmount(10d);
        order.setChargeTaxWay("0");//普通征收
        order.setInvoiceList("0");//不打印清单
        order.setInvoiceSplit("1");//拆票
        order.setInvoiceSfdy("0");//不立即打印
        order.setTaxMark("1");//金额含税
        order.setRemark("这是备注");

        //销方
        seller.setName("上海百旺测试3643");
        seller.setIdentifier("500102010003643");
        seller.setAddress("销方地址");
        seller.setTelephoneNo("110");
        seller.setBank("销方银行");
        seller.setBankAcc("123");

        //数据
        data.setDrawer("王亚辉");
        data.setVersion("19");
        data.setInvType("12");
        data.setOrder(order);
        data.setSeller(seller);

        post.setData(data);
        logger.info("抽取的数据=【"+JSON.toJSONString(post)+"】");
        return post;
    }
}

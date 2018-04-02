package com.rjxx.taxeasy.service;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.dto.*;
import org.apache.commons.codec.digest.DigestUtils;
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
        AdapterPost post = new AdapterPost();
        AdapterData data = new AdapterData();
        AdapterDataOrder order = new AdapterDataOrder();
        AdapterDataSeller seller = new AdapterDataSeller();
        AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
        List<AdapterDataOrderDetails> details = new ArrayList<>();
        List<AdapterDataOrderPayments> payments = new ArrayList<>();

        //数据
        data.setDrawer("王亚辉");
        data.setVersion("19");
        data.setInvType("12");
        data.setSerialNumber("20180323103125X");
        data.setOrder(order);
        data.setSeller(seller);

        //销方
        seller.setName("上海百旺测试3643");
        seller.setIdentifier("500102010003643");
        seller.setAddress("销方地址");
        seller.setTelephoneNo("110");
        seller.setBank("销方银行");
        seller.setBankAcc("123");

        //订单
        order.setBuyer(buyer);
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

        //购方
        buyer.setName("法国ankama信息技术有限公司");
        buyer.setIdentifier("500102010003643");
        buyer.setAddress("购方地址");
        buyer.setTelephoneNo("120");
        buyer.setBank("购方银行");
        buyer.setBankAcc("321");
        buyer.setCustomerType("1");
        buyer.setEmail("243409312@qq.com");
        buyer.setIsSend("1");

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

        //请求
        //辉通测试
        post.setAppId("RJ17634f1a0279");
        post.setTaxNo("500102010003643");
        String dataJson = JSON.toJSONString(data);
        System.out.println("data=" + dataJson);
        String key = "fa19f6c4d0e4144e8115ed71b0e4c349";
        String sign = DigestUtils.md5Hex("data=" + dataJson + "&key=" + key);
        System.out.println("sign=" + sign);
        post.setSign(sign);
        post.setData(data);
        post.setClientNo("test1");
        return post;
    }
}

package com.rjxx.taxeasy.wechat.service;

/**
 * Created by Administrator on 2017/9/26 0026.
 */
public interface SimpleInvoiceService {
    String getMsg(String appid,String sign,String orderData);

    String extractData(String extractCode);
}

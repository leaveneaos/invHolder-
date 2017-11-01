package com.rjxx.taxeasy.utils.alipay;

import com.alipay.api.AlipayObject;

/**
 * Created by xlm on 2017/11/1.
 */
public class AlipayBizInvoiceObject extends AlipayObject {

    private String einv_trade_id;

    private String timestamp;

    private String token;

    private String random;

    public String getEinv_trade_id() {
        return einv_trade_id;
    }

    public void setEinv_trade_id(String einv_trade_id) {
        this.einv_trade_id = einv_trade_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }
}

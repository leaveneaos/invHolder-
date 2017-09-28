package com.rjxx.taxeasy.wechat.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 税控操作返回值
 * Created by zhangbing on 2017-02-13.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Responese")
public class InvoiceResponse {

    //返回值
    @XmlElement(name = "ReturnCode")
    private String returnCode;

    //返回信息
    @XmlElement(name = "ReturnMessage")
    private String returnMessage;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }
}

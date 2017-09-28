package com.rjxx.taxeasy.wechat.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-27.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class OrderMain {

    private String OrderNo;

    private String InvoiceList = "0";

    private String InvoiceSplit = "1";

    private String InvoiceSfdy = "0";

    private String OrderDate;

    private String ChargeTaxWay = "0";

    private String TotalAmount;

    private String TaxMark = "1";

    private String Remark;

    private Buyer Buyer;

    private String ExtractedCode;

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(String orderNo) {
        OrderNo = orderNo;
    }

    public String getInvoiceList() {
        return InvoiceList;
    }

    public void setInvoiceList(String invoiceList) {
        InvoiceList = invoiceList;
    }

    public String getInvoiceSplit() {
        return InvoiceSplit;
    }

    public void setInvoiceSplit(String invoiceSplit) {
        InvoiceSplit = invoiceSplit;
    }

    public String getInvoiceSfdy() {
        return InvoiceSfdy;
    }

    public void setInvoiceSfdy(String invoiceSfdy) {
        InvoiceSfdy = invoiceSfdy;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }

    public String getChargeTaxWay() {
        return ChargeTaxWay;
    }

    public void setChargeTaxWay(String chargeTaxWay) {
        ChargeTaxWay = chargeTaxWay;
    }

    public String getTotalAmount() {
        return TotalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        TotalAmount = totalAmount;
    }

    public String getTaxMark() {
        return TaxMark;
    }

    public void setTaxMark(String taxMark) {
        TaxMark = taxMark;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public Buyer getBuyer() {
        return Buyer;
    }

    public void setBuyer(Buyer buyer) {
        Buyer = buyer;
    }

    public String getExtractedCode() {
        return ExtractedCode;
    }

    public void setExtractedCode(String extractedCode) {
        ExtractedCode = extractedCode;
    }
}

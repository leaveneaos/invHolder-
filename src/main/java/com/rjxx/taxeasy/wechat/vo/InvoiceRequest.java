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
@XmlRootElement(name = "Request")
public class InvoiceRequest {

    private String ClientNO;

    private String SerialNumber;

    private String InvType = "12";

    private String ServiceType = "0";

    private String Spbmbbh = "12.0";

    private String Drawer;

    private String Payee;

    private String Reviewer;

    private Seller Seller;

    @XmlElementWrapper(name = "OrderSize")
    private List<Order> Order;

    public String getClientNO() {
        return ClientNO;
    }

    public void setClientNO(String clientNO) {
        ClientNO = clientNO;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getInvType() {
        return InvType;
    }

    public void setInvType(String invType) {
        InvType = invType;
    }

    public String getServiceType() {
        return ServiceType;
    }

    public void setServiceType(String serviceType) {
        ServiceType = serviceType;
    }

    public String getSpbmbbh() {
        return Spbmbbh;
    }

    public void setSpbmbbh(String spbmbbh) {
        Spbmbbh = spbmbbh;
    }

    public String getDrawer() {
        return Drawer;
    }

    public void setDrawer(String drawer) {
        Drawer = drawer;
    }

    public String getPayee() {
        return Payee;
    }

    public void setPayee(String payee) {
        Payee = payee;
    }

    public String getReviewer() {
        return Reviewer;
    }

    public void setReviewer(String reviewer) {
        Reviewer = reviewer;
    }

    public Seller getSeller() {
        return Seller;
    }

    public void setSeller(Seller seller) {
        Seller = seller;
    }

    public List<Order> getOrder() {
        return Order;
    }

    public void setOrder(List<Order> order) {
        Order = order;
    }
}

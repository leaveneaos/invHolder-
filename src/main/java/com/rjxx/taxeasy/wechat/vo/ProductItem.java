package com.rjxx.taxeasy.wechat.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2017-05-27.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ProductItem {

    private String ProductCode;

    private String ProductName;

    private String RowType = "0";

    private String Spec;

    private String Unit;

    private String Quantity;

    private String UnitPrice;

    private String Amount;

    private String DeductAmount;

    private String TaxRate;

    private String TaxAmount;

    private String MxTotalAmount;

    private String VenderOwnCode;

    private String PolicyMark;

    private String TaxRateMark;

    private String PolicyName;

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getRowType() {
        return RowType;
    }

    public void setRowType(String rowType) {
        RowType = rowType;
    }

    public String getSpec() {
        return Spec;
    }

    public void setSpec(String spec) {
        Spec = spec;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getUnitPrice() {
        return UnitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        UnitPrice = unitPrice;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getDeductAmount() {
        return DeductAmount;
    }

    public void setDeductAmount(String deductAmount) {
        DeductAmount = deductAmount;
    }

    public String getTaxRate() {
        return TaxRate;
    }

    public void setTaxRate(String taxRate) {
        TaxRate = taxRate;
    }

    public String getTaxAmount() {
        return TaxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        TaxAmount = taxAmount;
    }

    public String getMxTotalAmount() {
        return MxTotalAmount;
    }

    public void setMxTotalAmount(String mxTotalAmount) {
        MxTotalAmount = mxTotalAmount;
    }

    public String getVenderOwnCode() {
        return VenderOwnCode;
    }

    public void setVenderOwnCode(String venderOwnCode) {
        VenderOwnCode = venderOwnCode;
    }

    public String getPolicyMark() {
        return PolicyMark;
    }

    public void setPolicyMark(String policyMark) {
        PolicyMark = policyMark;
    }

    public String getTaxRateMark() {
        return TaxRateMark;
    }

    public void setTaxRateMark(String taxRateMark) {
        TaxRateMark = taxRateMark;
    }

    public String getPolicyName() {
        return PolicyName;
    }

    public void setPolicyName(String policyName) {
        PolicyName = policyName;
    }
}

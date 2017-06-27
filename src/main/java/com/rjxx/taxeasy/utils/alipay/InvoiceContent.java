package com.rjxx.taxeasy.utils.alipay;

/**
 * Created by Administrator on 2017-06-26.
 */
public class InvoiceContent {

    private String item_name;

    private String item_no;

    private String item_price;

    private Double item_quantity;

    private Integer row_type;

    private String item_sum_price;

    private String item_tax_price;

    private String item_tax_rate;

    private String item_unit;

    private String item_amount;

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_no() {
        return item_no;
    }

    public void setItem_no(String item_no) {
        this.item_no = item_no;
    }

    public String getItem_price() {
        return item_price;
    }

    public void setItem_price(String item_price) {
        this.item_price = item_price;
    }

    public Double getItem_quantity() {
        return item_quantity;
    }

    public void setItem_quantity(Double item_quantity) {
        this.item_quantity = item_quantity;
    }

    public Integer getRow_type() {
        return row_type;
    }

    public void setRow_type(Integer row_type) {
        this.row_type = row_type;
    }

    public String getItem_sum_price() {
        return item_sum_price;
    }

    public void setItem_sum_price(String item_sum_price) {
        this.item_sum_price = item_sum_price;
    }

    public String getItem_tax_price() {
        return item_tax_price;
    }

    public void setItem_tax_price(String item_tax_price) {
        this.item_tax_price = item_tax_price;
    }

    public String getItem_tax_rate() {
        return item_tax_rate;
    }

    public void setItem_tax_rate(String item_tax_rate) {
        this.item_tax_rate = item_tax_rate;
    }

    public String getItem_unit() {
        return item_unit;
    }

    public void setItem_unit(String item_unit) {
        this.item_unit = item_unit;
    }

    public String getItem_amount() {
        return item_amount;
    }

    public void setItem_amount(String item_amount) {
        this.item_amount = item_amount;
    }
}

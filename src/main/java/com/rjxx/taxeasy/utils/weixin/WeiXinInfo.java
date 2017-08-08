package com.rjxx.taxeasy.utils.weixin;

import java.util.List;

/**
 * Created by Administrator on 2017-08-07.
 */
public class WeiXinInfo {

    private String title;

    private int fee;

    private  int billing_time;

    private  String billing_no;

    private  String billing_code;

    private List info;

    private  int fee_without_tax;

    private  int tax;

    private  String s_pdf_media_id;

    private String s_trip_pdf_media_id;

    private String check_code;

    private String buyer_number;

    private String buyer_address_and_phone;

    private String buyer_bank_account;

    private String seller_number;

    private String seller_address_and_phone;

    private  String seller_bank_account;

    private String remarks;

    private String cashier;

    private String maker;

    private String name;

    private int num;

    private String unit;

    private int price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getBilling_time() {
        return billing_time;
    }

    public void setBilling_time(int billing_time) {
        this.billing_time = billing_time;
    }

    public String getBilling_no() {
        return billing_no;
    }

    public void setBilling_no(String billing_no) {
        this.billing_no = billing_no;
    }

    public String getBilling_code() {
        return billing_code;
    }

    public void setBilling_code(String billing_code) {
        this.billing_code = billing_code;
    }

    public List getInfo() {
        return info;
    }

    public void setInfo(List info) {
        this.info = info;
    }

    public int getFee_without_tax() {
        return fee_without_tax;
    }

    public void setFee_without_tax(int fee_without_tax) {
        this.fee_without_tax = fee_without_tax;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public String getS_pdf_media_id() {
        return s_pdf_media_id;
    }

    public void setS_pdf_media_id(String s_pdf_media_id) {
        this.s_pdf_media_id = s_pdf_media_id;
    }

    public String getS_trip_pdf_media_id() {
        return s_trip_pdf_media_id;
    }

    public void setS_trip_pdf_media_id(String s_trip_pdf_media_id) {
        this.s_trip_pdf_media_id = s_trip_pdf_media_id;
    }

    public String getCheck_code() {
        return check_code;
    }

    public void setCheck_code(String check_code) {
        this.check_code = check_code;
    }

    public String getBuyer_number() {
        return buyer_number;
    }

    public void setBuyer_number(String buyer_number) {
        this.buyer_number = buyer_number;
    }

    public String getBuyer_address_and_phone() {
        return buyer_address_and_phone;
    }

    public void setBuyer_address_and_phone(String buyer_address_and_phone) {
        this.buyer_address_and_phone = buyer_address_and_phone;
    }

    public String getBuyer_bank_account() {
        return buyer_bank_account;
    }

    public void setBuyer_bank_account(String buyer_bank_account) {
        this.buyer_bank_account = buyer_bank_account;
    }

    public String getSeller_number() {
        return seller_number;
    }

    public void setSeller_number(String seller_number) {
        this.seller_number = seller_number;
    }

    public String getSeller_address_and_phone() {
        return seller_address_and_phone;
    }

    public void setSeller_address_and_phone(String seller_address_and_phone) {
        this.seller_address_and_phone = seller_address_and_phone;
    }

    public String getSeller_bank_account() {
        return seller_bank_account;
    }

    public void setSeller_bank_account(String seller_bank_account) {
        this.seller_bank_account = seller_bank_account;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCashier() {
        return cashier;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }
}

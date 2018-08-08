package com.rjxx.taxeasy.utils.alipay;

import java.util.List;

/**
 * @author: zsq
 * @date: 2018/8/7 15:52
 * @describe:
 */
public class InvoiceInfoList {

    private String user_id;

    private String apply_id;

    private String invoice_code;

    private String invoice_no;

    private String invoice_date;

    private String sum_amount;

    private String ex_tax_amount;

    private String tax_amount;

    private List<InvoiceContent> invoice_content;//发票明细

    private String out_trade_no;

    private String invoice_type;

    private String invoice_kind;

    private InvoiceTitle invoice_title;//发票抬头

    private String payee_register_no;

    private String payee_register_name;

    private String payee_address_tel;

    private String payee_bank_name_account;

    private String check_code;

    private String out_invoice_id;

    private String ori_blue_inv_code;

    private String ori_blue_inv_no;

    private String file_download_type;

    private String file_download_url;

    private String payee;

    private String checker;

    private String clerk;

    private String invoice_memo;

    private String extend_fields;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getApply_id() {
        return apply_id;
    }

    public void setApply_id(String apply_id) {
        this.apply_id = apply_id;
    }

    public String getInvoice_code() {
        return invoice_code;
    }

    public void setInvoice_code(String invoice_code) {
        this.invoice_code = invoice_code;
    }

    public String getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(String invoice_no) {
        this.invoice_no = invoice_no;
    }

    public String getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(String invoice_date) {
        this.invoice_date = invoice_date;
    }

    public String getSum_amount() {
        return sum_amount;
    }

    public void setSum_amount(String sum_amount) {
        this.sum_amount = sum_amount;
    }

    public String getEx_tax_amount() {
        return ex_tax_amount;
    }

    public void setEx_tax_amount(String ex_tax_amount) {
        this.ex_tax_amount = ex_tax_amount;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public void setTax_amount(String tax_amount) {
        this.tax_amount = tax_amount;
    }

    public List<InvoiceContent> getInvoice_content() {
        return invoice_content;
    }

    public void setInvoice_content(List<InvoiceContent> invoice_content) {
        this.invoice_content = invoice_content;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getInvoice_type() {
        return invoice_type;
    }

    public void setInvoice_type(String invoice_type) {
        this.invoice_type = invoice_type;
    }

    public String getInvoice_kind() {
        return invoice_kind;
    }

    public void setInvoice_kind(String invoice_kind) {
        this.invoice_kind = invoice_kind;
    }

    public InvoiceTitle getInvoice_title() {
        return invoice_title;
    }

    public void setInvoice_title(InvoiceTitle invoice_title) {
        this.invoice_title = invoice_title;
    }

    public String getPayee_register_no() {
        return payee_register_no;
    }

    public void setPayee_register_no(String payee_register_no) {
        this.payee_register_no = payee_register_no;
    }

    public String getPayee_register_name() {
        return payee_register_name;
    }

    public void setPayee_register_name(String payee_register_name) {
        this.payee_register_name = payee_register_name;
    }

    public String getPayee_address_tel() {
        return payee_address_tel;
    }

    public void setPayee_address_tel(String payee_address_tel) {
        this.payee_address_tel = payee_address_tel;
    }

    public String getPayee_bank_name_account() {
        return payee_bank_name_account;
    }

    public void setPayee_bank_name_account(String payee_bank_name_account) {
        this.payee_bank_name_account = payee_bank_name_account;
    }

    public String getCheck_code() {
        return check_code;
    }

    public void setCheck_code(String check_code) {
        this.check_code = check_code;
    }

    public String getOut_invoice_id() {
        return out_invoice_id;
    }

    public void setOut_invoice_id(String out_invoice_id) {
        this.out_invoice_id = out_invoice_id;
    }

    public String getOri_blue_inv_code() {
        return ori_blue_inv_code;
    }

    public void setOri_blue_inv_code(String ori_blue_inv_code) {
        this.ori_blue_inv_code = ori_blue_inv_code;
    }

    public String getOri_blue_inv_no() {
        return ori_blue_inv_no;
    }

    public void setOri_blue_inv_no(String ori_blue_inv_no) {
        this.ori_blue_inv_no = ori_blue_inv_no;
    }

    public String getFile_download_type() {
        return file_download_type;
    }

    public void setFile_download_type(String file_download_type) {
        this.file_download_type = file_download_type;
    }

    public String getFile_download_url() {
        return file_download_url;
    }

    public void setFile_download_url(String file_download_url) {
        this.file_download_url = file_download_url;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public String getClerk() {
        return clerk;
    }

    public void setClerk(String clerk) {
        this.clerk = clerk;
    }

    public String getInvoice_memo() {
        return invoice_memo;
    }

    public void setInvoice_memo(String invoice_memo) {
        this.invoice_memo = invoice_memo;
    }

    public String getExtend_fields() {
        return extend_fields;
    }

    public void setExtend_fields(String extend_fields) {
        this.extend_fields = extend_fields;
    }
}

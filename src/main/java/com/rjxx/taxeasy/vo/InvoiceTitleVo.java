package com.rjxx.taxeasy.vo;

/**
 * Created by Administrator on 2017-07-06.
 */
public class InvoiceTitleVo {

    private Boolean isDefault;

    private String openBankAccount;

    private String openBankName;

    private String taxRegisterNo;

    private String titleName;

    private String userAddress;

    private String userEmail;

    private String userMobile;

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public String getOpenBankAccount() {
        return openBankAccount;
    }

    public void setOpenBankAccount(String openBankAccount) {
        this.openBankAccount = openBankAccount;
    }

    public String getOpenBankName() {
        return openBankName;
    }

    public void setOpenBankName(String openBankName) {
        this.openBankName = openBankName;
    }

    public String getTaxRegisterNo() {
        return taxRegisterNo;
    }

    public void setTaxRegisterNo(String taxRegisterNo) {
        this.taxRegisterNo = taxRegisterNo;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }
}

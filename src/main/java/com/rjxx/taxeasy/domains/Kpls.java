package com.rjxx.taxeasy.domains;

import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.rjxx.comm.json.JsonDateFormat;
import com.rjxx.comm.json.JsonDatetimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * t_kpls 实体类
 * 开票流水表
 * 由GenEntityMysql类自动生成
 * Tue Oct 18 15:53:48 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_kpls")
public class Kpls  implements Serializable {

/**
 * 开票流水号
 */ 
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Integer kplsh;

/**
 * 单据号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 交易流水号
 */ 
@Column(name="jylsh")
	protected String jylsh;

@Column(name="jylssj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date jylssj;

/**
 * 发票种类代码
 */ 
@Column(name="fpzldm")
	protected String fpzldm;

/**
 * 发票操作类型代码 正常开具，红冲开具，空白作废，已开作废
 */ 
@Column(name="fpczlxdm")
	protected String fpczlxdm;

/**
 * 发票代码 发票代码
 */ 
@Column(name="fpdm")
	protected String fpdm;

/**
 * 发票号码 发票号码
 */ 
@Column(name="fphm")
	protected String fphm;

/**
 * 销方ID
 */ 
@Column(name="xfid")
	protected Integer xfid;

/**
 * 销方税号
 */ 
@Column(name="xfsh")
	protected String xfsh;

/**
 * 销方名称
 */ 
@Column(name="xfmc")
	protected String xfmc;

/**
 * 销方银行
 */ 
@Column(name="xfyh")
	protected String xfyh;

/**
 * 销方银行账号
 */ 
@Column(name="xfyhzh")
	protected String xfyhzh;

/**
 * 销方联系人
 */ 
@Column(name="xflxr")
	protected String xflxr;

/**
 * 销方地址
 */ 
@Column(name="xfdz")
	protected String xfdz;

/**
 * 销方电话
 */ 
@Column(name="xfdh")
	protected String xfdh;

/**
 * 销方邮编
 */ 
@Column(name="xfyb")
	protected String xfyb;

/**
 * 购方ID
 */ 
@Column(name="gfid")
	protected Integer gfid;

/**
 * 购方税号
 */ 
@Column(name="gfsh")
	protected String gfsh;

/**
 * 购方名称
 */ 
@Column(name="gfmc")
	protected String gfmc;

/**
 * 购方银行
 */ 
@Column(name="gfyh")
	protected String gfyh;

/**
 * 购方银行账号
 */ 
@Column(name="gfyhzh")
	protected String gfyhzh;

/**
 * 购方联系人
 */ 
@Column(name="gflxr")
	protected String gflxr;

/**
 * 购方地址
 */ 
@Column(name="gfdz")
	protected String gfdz;

/**
 * 购方电话
 */ 
@Column(name="gfdh")
	protected String gfdh;

/**
 * 购方邮政编码
 */ 
@Column(name="gfyb")
	protected String gfyb;

/**
 * 邮箱地址
 */ 
@Column(name="gfemail")
	protected String gfemail;

/**
 * 备注
 */ 
@Column(name="bz")
	protected String bz;

/**
 * 收款人
 */ 
@Column(name="skr")
	protected String skr;

/**
 * 开票人
 */ 
@Column(name="kpr")
	protected String kpr;

/**
 * 作废人
 */ 
@Column(name="zfr")
	protected String zfr;

/**
 * 复核人
 */ 
@Column(name="fhr")
	protected String fhr;

/**
 * 汇总开票序号 汇总开票序号
 */ 
@Column(name="hzkpxh")
	protected Integer hzkpxh;

/**
 * 所属月份
 */ 
@Column(name="ssyf")
	protected String ssyf;

/**
 * 专票红字通知单号
 */ 
@Column(name="hztzdh")
	protected String hztzdh;

/**
 * 红字(原)发票代码
 */ 
@Column(name="hzyfpdm")
	protected String hzyfpdm;

/**
 * 红字(原)发票号码
 */ 
@Column(name="hzyfphm")
	protected String hzyfphm;

/**
 * 换开标志 1、已换开；0、未换开
 */ 
@Column(name="hkbz")
	protected String hkbz;

/**
 * 换开发票代码
 */ 
@Column(name="hk_fpdm")
	protected String hkFpdm;

/**
 * 换开发票号码
 */ 
@Column(name="hk_fphm")
	protected String hkFphm;

/**
 * pdf地址
 */ 
@Column(name="pdfurl")
	protected String pdfurl;

/**
 * 税控设备码
 */ 
@Column(name="sksbm")
	protected String sksbm;

/**
 * 二维码
 */ 
@Column(name="ewm")
	protected String ewm;

/**
 * 校验码
 */ 
@Column(name="jym")
	protected String jym;

/**
 * 签名串
 */ 
@Column(name="qmc")
	protected String qmc;

/**
 * 密文区
 */ 
@Column(name="mwq")
	protected String mwq;

@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

/**
 * 录入人员
 */ 
@Column(name="lrry")
	protected Integer lrry;

@Column(name="xgsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date xgsj;

/**
 * 修改人员
 */ 
@Column(name="xgry")
	protected Integer xgry;

/**
 * 公司代码
 */ 
@Column(name="gsdm")
	protected String gsdm;

/**
 * 开票日期
 */ 
@Column(name="kprq")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date kprq;

/**
 * 作废日期
 */ 
@Column(name="zfrq")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date zfrq;

/**
 * 红冲日期
 */ 
@Column(name="hcrq")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date hcrq;

/**
 * 开票人员
 */ 
@Column(name="kpry")
	protected Integer kpry;

/**
 * 作废人员
 */ 
@Column(name="zfry")
	protected Integer zfry;

/**
 * 红冲人员
 */ 
@Column(name="hcry")
	protected Integer hcry;

/**
 * 开票点代码
 */ 
@Column(name="kpddm")
	protected String kpddm;

/**
 * 税控盘号
 */ 
@Column(name="skph")
	protected String skph;

/**
 * 价税合计
 */ 
@Column(name="jshj")
	protected Double jshj;

/**
 * 合计金额
 */ 
@Column(name="hjje")
	protected Double hjje;

/**
 * 合计税额
 */ 
@Column(name="hjse")
	protected Double hjse;

/**
 * 打印标识，1：已打印，0：未打印
 */ 
@Column(name="printflag")
	protected String printflag;

/**
 * 有效标志：0，无效；1，有效
 */ 
@Column(name="yxbz")
	protected String yxbz;


	public Integer getKplsh(){
		return kplsh;
	}

	public void setKplsh(Integer kplsh){
		this.kplsh=kplsh;
	}

	public Integer getDjh(){
		return djh;
	}

	public void setDjh(Integer djh){
		this.djh=djh;
	}

	public String getJylsh(){
		return jylsh;
	}

	public void setJylsh(String jylsh){
		this.jylsh=jylsh;
	}

	public Date getJylssj(){
		return jylssj;
	}

	public void setJylssj(Date jylssj){
		this.jylssj=jylssj;
	}

	public String getFpzldm(){
		return fpzldm;
	}

	public void setFpzldm(String fpzldm){
		this.fpzldm=fpzldm;
	}

	public String getFpczlxdm(){
		return fpczlxdm;
	}

	public void setFpczlxdm(String fpczlxdm){
		this.fpczlxdm=fpczlxdm;
	}

	public String getFpdm(){
		return fpdm;
	}

	public void setFpdm(String fpdm){
		this.fpdm=fpdm;
	}

	public String getFphm(){
		return fphm;
	}

	public void setFphm(String fphm){
		this.fphm=fphm;
	}

	public Integer getXfid(){
		return xfid;
	}

	public void setXfid(Integer xfid){
		this.xfid=xfid;
	}

	public String getXfsh(){
		return xfsh;
	}

	public void setXfsh(String xfsh){
		this.xfsh=xfsh;
	}

	public String getXfmc(){
		return xfmc;
	}

	public void setXfmc(String xfmc){
		this.xfmc=xfmc;
	}

	public String getXfyh(){
		return xfyh;
	}

	public void setXfyh(String xfyh){
		this.xfyh=xfyh;
	}

	public String getXfyhzh(){
		return xfyhzh;
	}

	public void setXfyhzh(String xfyhzh){
		this.xfyhzh=xfyhzh;
	}

	public String getXflxr(){
		return xflxr;
	}

	public void setXflxr(String xflxr){
		this.xflxr=xflxr;
	}

	public String getXfdz(){
		return xfdz;
	}

	public void setXfdz(String xfdz){
		this.xfdz=xfdz;
	}

	public String getXfdh(){
		return xfdh;
	}

	public void setXfdh(String xfdh){
		this.xfdh=xfdh;
	}

	public String getXfyb(){
		return xfyb;
	}

	public void setXfyb(String xfyb){
		this.xfyb=xfyb;
	}

	public Integer getGfid(){
		return gfid;
	}

	public void setGfid(Integer gfid){
		this.gfid=gfid;
	}

	public String getGfsh(){
		return gfsh;
	}

	public void setGfsh(String gfsh){
		this.gfsh=gfsh;
	}

	public String getGfmc(){
		return gfmc;
	}

	public void setGfmc(String gfmc){
		this.gfmc=gfmc;
	}

	public String getGfyh(){
		return gfyh;
	}

	public void setGfyh(String gfyh){
		this.gfyh=gfyh;
	}

	public String getGfyhzh(){
		return gfyhzh;
	}

	public void setGfyhzh(String gfyhzh){
		this.gfyhzh=gfyhzh;
	}

	public String getGflxr(){
		return gflxr;
	}

	public void setGflxr(String gflxr){
		this.gflxr=gflxr;
	}

	public String getGfdz(){
		return gfdz;
	}

	public void setGfdz(String gfdz){
		this.gfdz=gfdz;
	}

	public String getGfdh(){
		return gfdh;
	}

	public void setGfdh(String gfdh){
		this.gfdh=gfdh;
	}

	public String getGfyb(){
		return gfyb;
	}

	public void setGfyb(String gfyb){
		this.gfyb=gfyb;
	}

	public String getGfemail(){
		return gfemail;
	}

	public void setGfemail(String gfemail){
		this.gfemail=gfemail;
	}

	public String getBz(){
		return bz;
	}

	public void setBz(String bz){
		this.bz=bz;
	}

	public String getSkr(){
		return skr;
	}

	public void setSkr(String skr){
		this.skr=skr;
	}

	public String getKpr(){
		return kpr;
	}

	public void setKpr(String kpr){
		this.kpr=kpr;
	}

	public String getZfr(){
		return zfr;
	}

	public void setZfr(String zfr){
		this.zfr=zfr;
	}

	public String getFhr(){
		return fhr;
	}

	public void setFhr(String fhr){
		this.fhr=fhr;
	}

	public Integer getHzkpxh(){
		return hzkpxh;
	}

	public void setHzkpxh(Integer hzkpxh){
		this.hzkpxh=hzkpxh;
	}

	public String getSsyf(){
		return ssyf;
	}

	public void setSsyf(String ssyf){
		this.ssyf=ssyf;
	}

	public String getHztzdh(){
		return hztzdh;
	}

	public void setHztzdh(String hztzdh){
		this.hztzdh=hztzdh;
	}

	public String getHzyfpdm(){
		return hzyfpdm;
	}

	public void setHzyfpdm(String hzyfpdm){
		this.hzyfpdm=hzyfpdm;
	}

	public String getHzyfphm(){
		return hzyfphm;
	}

	public void setHzyfphm(String hzyfphm){
		this.hzyfphm=hzyfphm;
	}

	public String getHkbz(){
		return hkbz;
	}

	public void setHkbz(String hkbz){
		this.hkbz=hkbz;
	}

	public String getHkFpdm(){
		return hkFpdm;
	}

	public void setHkFpdm(String hkFpdm){
		this.hkFpdm=hkFpdm;
	}

	public String getHkFphm(){
		return hkFphm;
	}

	public void setHkFphm(String hkFphm){
		this.hkFphm=hkFphm;
	}

	public String getPdfurl(){
		return pdfurl;
	}

	public void setPdfurl(String pdfurl){
		this.pdfurl=pdfurl;
	}

	public String getSksbm(){
		return sksbm;
	}

	public void setSksbm(String sksbm){
		this.sksbm=sksbm;
	}

	public String getEwm(){
		return ewm;
	}

	public void setEwm(String ewm){
		this.ewm=ewm;
	}

	public String getJym(){
		return jym;
	}

	public void setJym(String jym){
		this.jym=jym;
	}

	public String getQmc(){
		return qmc;
	}

	public void setQmc(String qmc){
		this.qmc=qmc;
	}

	public String getMwq(){
		return mwq;
	}

	public void setMwq(String mwq){
		this.mwq=mwq;
	}

	public Date getLrsj(){
		return lrsj;
	}

	public void setLrsj(Date lrsj){
		this.lrsj=lrsj;
	}

	public Integer getLrry(){
		return lrry;
	}

	public void setLrry(Integer lrry){
		this.lrry=lrry;
	}

	public Date getXgsj(){
		return xgsj;
	}

	public void setXgsj(Date xgsj){
		this.xgsj=xgsj;
	}

	public Integer getXgry(){
		return xgry;
	}

	public void setXgry(Integer xgry){
		this.xgry=xgry;
	}

	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

	public Date getKprq(){
		return kprq;
	}

	public void setKprq(Date kprq){
		this.kprq=kprq;
	}

	public Date getZfrq(){
		return zfrq;
	}

	public void setZfrq(Date zfrq){
		this.zfrq=zfrq;
	}

	public Date getHcrq(){
		return hcrq;
	}

	public void setHcrq(Date hcrq){
		this.hcrq=hcrq;
	}

	public Integer getKpry(){
		return kpry;
	}

	public void setKpry(Integer kpry){
		this.kpry=kpry;
	}

	public Integer getZfry(){
		return zfry;
	}

	public void setZfry(Integer zfry){
		this.zfry=zfry;
	}

	public Integer getHcry(){
		return hcry;
	}

	public void setHcry(Integer hcry){
		this.hcry=hcry;
	}

	public String getKpddm(){
		return kpddm;
	}

	public void setKpddm(String kpddm){
		this.kpddm=kpddm;
	}

	public String getSkph(){
		return skph;
	}

	public void setSkph(String skph){
		this.skph=skph;
	}

	public Double getJshj(){
		return jshj;
	}

	public void setJshj(Double jshj){
		this.jshj=jshj;
	}

	public Double getHjje(){
		return hjje;
	}

	public void setHjje(Double hjje){
		this.hjje=hjje;
	}

	public Double getHjse(){
		return hjse;
	}

	public void setHjse(Double hjse){
		this.hjse=hjse;
	}

	public String getPrintflag(){
		return printflag;
	}

	public void setPrintflag(String printflag){
		this.printflag=printflag;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

}


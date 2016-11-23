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
 * t_kpspmx 实体类
 * 开票商品明细表
 * 由GenEntityMysql类自动生成
 * Thu Nov 17 10:06:33 GMT+08:00 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_kpspmx")
public class Kpspmx  implements Serializable {

/**
 * 开票流水号
 */ 
@Column(name="kplsh")
	protected Integer kplsh;

/**
 * 单据号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 商品明细序号
 */ 
@Column(name="spmxxh")
	protected Integer spmxxh;

/**
 * 发票行性质 0、正常行；1、折扣行；2、被折扣行
 */ 
@Column(name="fphxz")
	protected String fphxz;

/**
 * 商品代码
 */ 
@Column(name="spdm")
	protected String spdm;

/**
 * 商品名称
 */ 
@Column(name="spmc")
	protected String spmc;

/**
 * 商品规格型号
 */ 
@Column(name="spggxh")
	protected String spggxh;

/**
 * 商品单位
 */ 
@Column(name="spdw")
	protected String spdw;

/**
 * 商品数量
 */ 
@Column(name="sps")
	protected Double sps;

/**
 * 商品单价
 */ 
@Column(name="spdj")
	protected Double spdj;

/**
 * 商品金额
 */ 
@Column(name="spje")
	protected Double spje;

/**
 * 商品税率
 */ 
@Column(name="spsl")
	protected Double spsl;

/**
 * 商品税额
 */ 
@Column(name="spse")
	protected Double spse;

/**
 * 发票代码
 */ 
@Column(name="fpdm")
	protected String fpdm;

/**
 * 发票号码
 */ 
@Column(name="fphm")
	protected String fphm;

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
 * 录入时间
 */ 
@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

/**
 * 录入人员
 */ 
@Column(name="lrry")
	protected Integer lrry;

/**
 * 修改时间
 */ 
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

@Column(name="khcje")
	protected Double khcje;

@Column(name="yhcje")
	protected Double yhcje;

/**
 * 主键
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

@Column(name="skpid")
	protected Integer skpid;


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

	public Integer getSpmxxh(){
		return spmxxh;
	}

	public void setSpmxxh(Integer spmxxh){
		this.spmxxh=spmxxh;
	}

	public String getFphxz(){
		return fphxz;
	}

	public void setFphxz(String fphxz){
		this.fphxz=fphxz;
	}

	public String getSpdm(){
		return spdm;
	}

	public void setSpdm(String spdm){
		this.spdm=spdm;
	}

	public String getSpmc(){
		return spmc;
	}

	public void setSpmc(String spmc){
		this.spmc=spmc;
	}

	public String getSpggxh(){
		return spggxh;
	}

	public void setSpggxh(String spggxh){
		this.spggxh=spggxh;
	}

	public String getSpdw(){
		return spdw;
	}

	public void setSpdw(String spdw){
		this.spdw=spdw;
	}

	public Double getSps(){
		return sps;
	}

	public void setSps(Double sps){
		this.sps=sps;
	}

	public Double getSpdj(){
		return spdj;
	}

	public void setSpdj(Double spdj){
		this.spdj=spdj;
	}

	public Double getSpje(){
		return spje;
	}

	public void setSpje(Double spje){
		this.spje=spje;
	}

	public Double getSpsl(){
		return spsl;
	}

	public void setSpsl(Double spsl){
		this.spsl=spsl;
	}

	public Double getSpse(){
		return spse;
	}

	public void setSpse(Double spse){
		this.spse=spse;
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

	public Double getKhcje(){
		return khcje;
	}

	public void setKhcje(Double khcje){
		this.khcje=khcje;
	}

	public Double getYhcje(){
		return yhcje;
	}

	public void setYhcje(Double yhcje){
		this.yhcje=yhcje;
	}

	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public Integer getSkpid(){
		return skpid;
	}

	public void setSkpid(Integer skpid){
		this.skpid=skpid;
	}

}


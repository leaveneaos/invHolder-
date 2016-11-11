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
 * t_xf 实体类
 * 销方信息表
 * 由GenEntityMysql类自动生成
 * Thu Nov 10 12:48:24 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_xf")
public class Xf  implements Serializable {

/**
 * 销方ID
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 机构编码
 */ 
@Column(name="jgbm")
	protected String jgbm;

/**
 * 上级机构编码
 */ 
@Column(name="sjjgbm")
	protected String sjjgbm;

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
 * 授权用户数
 */ 
@Column(name="sqyhs")
	protected Integer sqyhs;

/**
 * 收款人
 */ 
@Column(name="skr")
	protected String skr;

/**
 * 复核人
 */ 
@Column(name="fhr")
	protected String fhr;

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
 * 状态标识 0：待复核；1：已生效；2：已失效
 */ 
@Column(name="yxbz")
	protected String yxbz;

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
 * 开票最大金额
 */ 
@Column(name="kpzdje")
	protected Double kpzdje;

/**
 * 公司代码
 */ 
@Column(name="gsdm")
	protected String gsdm;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getJgbm(){
		return jgbm;
	}

	public void setJgbm(String jgbm){
		this.jgbm=jgbm;
	}

	public String getSjjgbm(){
		return sjjgbm;
	}

	public void setSjjgbm(String sjjgbm){
		this.sjjgbm=sjjgbm;
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

	public Integer getSqyhs(){
		return sqyhs;
	}

	public void setSqyhs(Integer sqyhs){
		this.sqyhs=sqyhs;
	}

	public String getSkr(){
		return skr;
	}

	public void setSkr(String skr){
		this.skr=skr;
	}

	public String getFhr(){
		return fhr;
	}

	public void setFhr(String fhr){
		this.fhr=fhr;
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

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
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

	public Double getKpzdje(){
		return kpzdje;
	}

	public void setKpzdje(Double kpzdje){
		this.kpzdje=kpzdje;
	}

	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

}


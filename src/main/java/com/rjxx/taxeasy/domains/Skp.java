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
 * t_skp 实体类
 * 税控盘信息表
 * 由GenEntityMysql类自动生成
 * Thu Nov 10 12:49:39 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_skp")
public class Skp  implements Serializable {

/**
 * 税控盘号
 */ 
@Column(name="skph")
	protected String skph;

/**
 * 税控盘密码
 */ 
@Column(name="skpmm")
	protected String skpmm;

/**
 * 证书密码
 */ 
@Column(name="zsmm")
	protected String zsmm;

@Column(name="zcm")
	protected String zcm;

/**
 * 发票库存预警阈值
 */ 
@Column(name="fpyz")
	protected Integer fpyz;

/**
 * 备注
 */ 
@Column(name="bz")
	protected String bz;

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
 * 销方id
 */ 
@Column(name="xfid")
	protected Integer xfid;

/**
 * 公司代码
 */ 
@Column(name="gsdm")
	protected String gsdm;

/**
 * 开票点名称
 */ 
@Column(name="kpdmc")
	protected String kpdmc;

/**
 * 有效标志：0，无效；1，有效
 */ 
@Column(name="yxbz")
	protected String yxbz;

/**
 * 电子发票最大开票限额
 */ 
@Column(name="dpmax")
	protected Double dpmax;

/**
 * 地址发票开票阈值
 */ 
@Column(name="fpfz")
	protected Double fpfz;

/**
 * 普票开票最大限额
 */ 
@Column(name="ppmax")
	protected Double ppmax;

/**
 * 普票开票阈值
 */ 
@Column(name="ppfz")
	protected Double ppfz;

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 专票最大开票限额
 */ 
@Column(name="zpmax")
	protected Double zpmax;

/**
 * 专票阈值
 */ 
@Column(name="zpfz")
	protected Double zpfz;

/**
 * 开票点ip地址
 */ 
@Column(name="kpdip")
	protected String kpdip;
@Column(name="kpddm")
protected String kpddm;

	public String getSkph(){
		return skph;
	}

	public void setSkph(String skph){
		this.skph=skph;
	}

	public String getSkpmm(){
		return skpmm;
	}

	public void setSkpmm(String skpmm){
		this.skpmm=skpmm;
	}

	public String getZsmm(){
		return zsmm;
	}

	public void setZsmm(String zsmm){
		this.zsmm=zsmm;
	}

	public String getZcm(){
		return zcm;
	}

	public void setZcm(String zcm){
		this.zcm=zcm;
	}

	public Integer getFpyz(){
		return fpyz;
	}

	public void setFpyz(Integer fpyz){
		this.fpyz=fpyz;
	}

	public String getBz(){
		return bz;
	}

	public void setBz(String bz){
		this.bz=bz;
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

	public Integer getXfid(){
		return xfid;
	}

	public void setXfid(Integer xfid){
		this.xfid=xfid;
	}

	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

	public String getKpdmc(){
		return kpdmc;
	}

	public void setKpdmc(String kpdmc){
		this.kpdmc=kpdmc;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

	public Double getDpmax(){
		return dpmax;
	}

	public void setDpmax(Double dpmax){
		this.dpmax=dpmax;
	}

	public Double getFpfz(){
		return fpfz;
	}

	public void setFpfz(Double fpfz){
		this.fpfz=fpfz;
	}

	public Double getPpmax(){
		return ppmax;
	}

	public void setPpmax(Double ppmax){
		this.ppmax=ppmax;
	}

	public Double getPpfz(){
		return ppfz;
	}

	public void setPpfz(Double ppfz){
		this.ppfz=ppfz;
	}

	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public Double getZpmax(){
		return zpmax;
	}

	public void setZpmax(Double zpmax){
		this.zpmax=zpmax;
	}

	public Double getZpfz(){
		return zpfz;
	}

	public void setZpfz(Double zpfz){
		this.zpfz=zpfz;
	}

	public String getKpdip(){
		return kpdip;
	}

	public void setKpdip(String kpdip){
		this.kpdip=kpdip;
	}

	public String getKpddm() {
		return kpddm;
	}

	public void setKpddm(String kpddm) {
		this.kpddm = kpddm;
	}

}


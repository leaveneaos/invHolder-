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
 * t_jyxx 实体类
 * 交易信息表
 * 由GenEntityMysql类自动生成
 * Tue Nov 22 10:36:11 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_jyxx")
public class Jyxx  implements Serializable {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

@Column(name="order_no")
	protected String orderNo;

@Column(name="order_time")
	protected String orderTime;

@Column(name="price")
	protected Double price;

@Column(name="sign")
	protected String sign;

@Column(name="store_no")
	protected String storeNo;

@Column(name="gsdm")
	protected String gsdm;

@Column(name="lrry")
	protected String lrry;

@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

@Column(name="xgry")
	protected String xgry;

@Column(name="xgsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date xgsj;

@Column(name="yxbz")
	protected String yxbz;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getOrderNo(){
		return orderNo;
	}

	public void setOrderNo(String orderNo){
		this.orderNo=orderNo;
	}

	public String getOrderTime(){
		return orderTime;
	}

	public void setOrderTime(String orderTime){
		this.orderTime=orderTime;
	}

	public Double getPrice(){
		return price;
	}

	public void setPrice(Double price){
		this.price=price;
	}

	public String getSign(){
		return sign;
	}

	public void setSign(String sign){
		this.sign=sign;
	}

	public String getStoreNo(){
		return storeNo;
	}

	public void setStoreNo(String storeNo){
		this.storeNo=storeNo;
	}

	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

	public String getLrry(){
		return lrry;
	}

	public void setLrry(String lrry){
		this.lrry=lrry;
	}

	public Date getLrsj(){
		return lrsj;
	}

	public void setLrsj(Date lrsj){
		this.lrsj=lrsj;
	}

	public String getXgry(){
		return xgry;
	}

	public void setXgry(String xgry){
		this.xgry=xgry;
	}

	public Date getXgsj(){
		return xgsj;
	}

	public void setXgsj(Date xgsj){
		this.xgsj=xgsj;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

}


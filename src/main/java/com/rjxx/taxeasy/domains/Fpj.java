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
 * t_fpj 实体类
 * 由GenEntityMysql类自动生成
 * Thu Nov 03 14:26:00 GMT+08:00 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_fpj")
public class Fpj  implements Serializable {

/**
 * 主键id
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 开票流水号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 微信id
 */ 
@Column(name="unionid")
	protected String unionid;

/**
 * 有效标志：0，无效；1，有效
 */ 
@Column(name="yxbz")
	protected String yxbz;

/**
 * 录入人员
 */ 
@Column(name="lrry")
	protected Integer lrry;

/**
 * 录入时间
 */ 
@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

/**
 * 修改人员
 */ 
@Column(name="xgry")
	protected Integer xgry;

/**
 * 修改时间
 */ 
@Column(name="xgsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date xgsj;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public Integer getDjh() {
		return djh;
	}

	public void setDjh(Integer djh) {
		this.djh = djh;
	}

	public String getUnionid(){
		return unionid;
	}

	public void setUnionid(String unionid){
		this.unionid=unionid;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

	public Integer getLrry(){
		return lrry;
	}

	public void setLrry(Integer lrry){
		this.lrry=lrry;
	}

	public Date getLrsj(){
		return lrsj;
	}

	public void setLrsj(Date lrsj){
		this.lrsj=lrsj;
	}

	public Integer getXgry(){
		return xgry;
	}

	public void setXgry(Integer xgry){
		this.xgry=xgry;
	}

	public Date getXgsj(){
		return xgsj;
	}

	public void setXgsj(Date xgsj){
		this.xgsj=xgsj;
	}

}


package com.rjxx.taxeasy.domains;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.rjxx.comm.json.JsonDateFormat;
import com.rjxx.comm.json.JsonDatetimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * t_zzfpqp 实体类
 * 由GenEntityMysql类自动生成
 * Thu Nov 17 16:46:29 GMT+08:00 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_zzfpqp")
public class Zzfpqp  implements Serializable {

/**
 * 主键id
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 取票方式
 */ 
@Column(name="qpfs")
	protected String qpfs;

/**
 * 单据号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 收件人
 */ 
@Column(name="sjr")
	protected String sjr;

/**
 * 单位名称
 */ 
@Column(name="dwmc")
	protected String dwmc;

/**
 * 收件地址
 */ 
@Column(name="sjdz")
	protected String sjdz;

/**
 * 邮编
 */ 
@Column(name="yb")
	protected String yb;

/**
 * 联系电话
 */ 
@Column(name="lxdh")
	protected String lxdh;

/**
 * 自提店面
 */ 
@Column(name="ztdm")
	protected String ztdm;

/**
 * 起始时间
 */ 
@Column(name="qssj")
	protected String qssj;

/**
 * 结束时间
 */ 
@Column(name="jssj")
	protected String jssj;

/**
 * 有效标志：0，无效；1，有效
 */ 
@Column(name="yxbz")
	protected String yxbz;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getQpfs(){
		return qpfs;
	}

	public void setQpfs(String qpfs){
		this.qpfs=qpfs;
	}

	public Integer getDjh(){
		return djh;
	}

	public void setDjh(Integer djh){
		this.djh=djh;
	}

	public String getSjr(){
		return sjr;
	}

	public void setSjr(String sjr){
		this.sjr=sjr;
	}

	public String getDwmc(){
		return dwmc;
	}

	public void setDwmc(String dwmc){
		this.dwmc=dwmc;
	}

	public String getSjdz(){
		return sjdz;
	}

	public void setSjdz(String sjdz){
		this.sjdz=sjdz;
	}

	public String getYb(){
		return yb;
	}

	public void setYb(String yb){
		this.yb=yb;
	}

	public String getLxdh(){
		return lxdh;
	}

	public void setLxdh(String lxdh){
		this.lxdh=lxdh;
	}

	public String getZtdm(){
		return ztdm;
	}

	public void setZtdm(String ztdm){
		this.ztdm=ztdm;
	}

	public String getQssj(){
		return qssj;
	}

	public void setQssj(String qssj){
		this.qssj=qssj;
	}

	public String getJssj(){
		return jssj;
	}

	public void setJssj(String jssj){
		this.jssj=jssj;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

}


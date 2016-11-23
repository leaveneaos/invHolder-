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
 * t_dzfplog 实体类
 * 电子发票日志表
 * 由GenEntityMysql类自动生成
 * Thu Nov 17 10:05:02 GMT+08:00 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_dzfplog")
public class Dzfplog  implements Serializable {

/**
 * 日志id
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer rzid;

/**
 * 单据号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 电子发票处理状态代码
 */ 
@Column(name="clztdm")
	protected String clztdm;

/**
 * 电子发票处理结果代码
 */ 
@Column(name="cljgdm")
	protected String cljgdm;

/**
 * 调用方法和处理参数 调用方法名及传入参数
 */ 
@Column(name="ffcs")
	protected String ffcs;

/**
 * 异常描述
 */ 
@Column(name="ycms")
	protected String ycms;

@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

/**
 * 录入人员
 */ 
@Column(name="lrry")
	protected Integer lrry;

@Column(name="xfsh")
	protected String xfsh;

@Column(name="jylsh")
	protected String jylsh;


	public Integer getRzid(){
		return rzid;
	}

	public void setRzid(Integer rzid){
		this.rzid=rzid;
	}

	public Integer getDjh(){
		return djh;
	}

	public void setDjh(Integer djh){
		this.djh=djh;
	}

	public String getClztdm(){
		return clztdm;
	}

	public void setClztdm(String clztdm){
		this.clztdm=clztdm;
	}

	public String getCljgdm(){
		return cljgdm;
	}

	public void setCljgdm(String cljgdm){
		this.cljgdm=cljgdm;
	}

	public String getFfcs(){
		return ffcs;
	}

	public void setFfcs(String ffcs){
		this.ffcs=ffcs;
	}

	public String getYcms(){
		return ycms;
	}

	public void setYcms(String ycms){
		this.ycms=ycms;
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

	public String getXfsh(){
		return xfsh;
	}

	public void setXfsh(String xfsh){
		this.xfsh=xfsh;
	}

	public String getJylsh(){
		return jylsh;
	}

	public void setJylsh(String jylsh){
		this.jylsh=jylsh;
	}

}


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
 * t_yh 实体类
 * 用户表
 * 由GenEntityMysql类自动生成
 * Thu Nov 10 14:37:36 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_yh")
public class Yh  implements Serializable {

/**
 * 用户ID
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 登录用户名 登录用户名唯一
 */ 
@Column(name="dlyhid")
	protected String dlyhid;

/**
 * 用户名称
 */ 
@Column(name="yhmc")
	protected String yhmc;

/**
 * 用户口令
 */ 
@Column(name="yhmm")
	protected String yhmm;

/**
 * 用户机构
 */ 
@Column(name="yhjg")
	protected Integer yhjg;

/**
 * 公司代码
 */ 
@Column(name="gsdm")
	protected String gsdm;

/**
 * 性别 1、男；0、女
 */ 
@Column(name="xb")
	protected String xb;

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
 * 授权key
 */ 
@Column(name="appkey")
	protected String appkey;

/**
 * 授权秘钥
 */ 
@Column(name="secret")
	protected String secret;

/**
 * 角色id
 */ 
@Column(name="roleids")
	protected String roleids;

/**
 * 状态标识
 */ 
@Column(name="ztbz")
	protected String ztbz;

/**
 * 是否为超级管理员
 */ 
@Column(name="sup")
	protected String sup;

/**
 * 手机号码
 */ 
@Column(name="sjhm")
	protected String sjhm;

/**
 * 邮箱
 */ 
@Column(name="yx")
	protected String yx;

/**
 * 有效标志
 */ 
@Column(name="yxbz")
	protected String yxbz;

@Column(name="admin")
	protected String admin;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getDlyhid(){
		return dlyhid;
	}

	public void setDlyhid(String dlyhid){
		this.dlyhid=dlyhid;
	}

	public String getYhmc(){
		return yhmc;
	}

	public void setYhmc(String yhmc){
		this.yhmc=yhmc;
	}

	public String getYhmm(){
		return yhmm;
	}

	public void setYhmm(String yhmm){
		this.yhmm=yhmm;
	}

	public Integer getYhjg(){
		return yhjg;
	}

	public void setYhjg(Integer yhjg){
		this.yhjg=yhjg;
	}

	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

	public String getXb(){
		return xb;
	}

	public void setXb(String xb){
		this.xb=xb;
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

	public String getAppkey(){
		return appkey;
	}

	public void setAppkey(String appkey){
		this.appkey=appkey;
	}

	public String getSecret(){
		return secret;
	}

	public void setSecret(String secret){
		this.secret=secret;
	}

	public String getRoleids(){
		return roleids;
	}

	public void setRoleids(String roleids){
		this.roleids=roleids;
	}

	public String getZtbz(){
		return ztbz;
	}

	public void setZtbz(String ztbz){
		this.ztbz=ztbz;
	}

	public String getSup(){
		return sup;
	}

	public void setSup(String sup){
		this.sup=sup;
	}

	public String getSjhm(){
		return sjhm;
	}

	public void setSjhm(String sjhm){
		this.sjhm=sjhm;
	}

	public String getYx(){
		return yx;
	}

	public void setYx(String yx){
		this.yx=yx;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

	public String getAdmin(){
		return admin;
	}

	public void setAdmin(String admin){
		this.admin=admin;
	}

}

